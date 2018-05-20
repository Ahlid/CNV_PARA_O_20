package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.LinkedHashMap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;


public class Proxy {
    final static Logger logger = Logger.getLogger(Proxy.class);
    
    private HttpServer proxiedServer = null;
    private static int PROXY_PORT = 8080;
    
    private static Balancer balancer = null;
    private static Scaler scaler = null;
    
    public Proxy() throws Exception {
        balancer = new LoadBalancer();
        scaler = new Scaler(balancer);
        balancer.setScaler(scaler);
        
    }
    
    public static void main(String[] args) {
        
        try {
            
            Proxy proxy = new Proxy();
            proxy.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Oops at proxy: " + e.getMessage());
            System.out.println("Probably problems with ~/.aws/credentials file!");
        }
    }
    
    
    private void terminate() throws Exception {
        logger.info("Stopping Proxy...");
        scaler.setState(false);
        scaler.join();
        proxiedServer.stop(1);
        logger.info("Proxy Stopped.");
    }
    
    public void start() throws Exception {
        logger.info("Launching Scaler...");
        scaler.start();
        
        logger.info("Launching Proxy at port " + PROXY_PORT);
        proxiedServer = HttpServer.create(new InetSocketAddress(PROXY_PORT), 0);
        proxiedServer.createContext("/", new HandleRequest(scaler, balancer));
        proxiedServer.createContext("/lb", new MyStatusHandler());
        proxiedServer.createContext("/setup", new MySetupHandler());
        proxiedServer.setExecutor(Executors.newCachedThreadPool());
        proxiedServer.start();
        
        logger.info("Proxy started.");
        
        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(scaler.sayHello, 0, 30000);
    }
    
    static class MySetupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            
            // Process URL query string
            String query = t.getRequestURI().getQuery();
            String[] paramList = query.split("&");
            
            // Store query string parameters in LinkedHashMap(preserving insertion order)
            for (String param : paramList) {
                String paramName = param.split("=")[0];
                String paramValue = param.split("=")[1];
                params.put(paramName, paramValue);
            }
            scaler.setupConfig(params);
            
            String response = "This was the query:" + t.getRequestURI().getQuery() 
            + "##";
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class MyStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            
            String style = "<style>" +
            "table, th, td {" +
            "border: 1px solid black;" +
            "border-collapse: collapse;}" +
            "th, td {" +
            "padding: 5px;" +
            "text-align: left;}" +
            "</style>";
            
            String configHeader = "<tr><th>AMI_ID</th>" + 
            "<th>Min_Workers</th>" + 
            "<th>Max_Workers</th>" +
            "<th>Submit</th></tr>";
            LinkedHashMap<String,String> configs =  scaler.getConfigs();
            String configsStr = "<tr><form action='/setup'> <th> " +
            "<input type='text' name='AMI_Name' value='"+ configs.get("AMI_Name") + "'> </th>" + 
            "<th><input type='text' name='MIN_WORKERS' value='"+ configs.get("MIN_WORKERS") + "'> </th>" + 
            "<th><input type='text' name='MAX_WORKERS' value='"+ configs.get("MAX_WORKERS") + "'> </th>" +
            "<th><input type='submit' value='Submit'></th></form></tr>";
            
            String instanceHeader = "<tr><th>ID</th>" + 
            "<th>Status</th>" + 
            "<th>Progress</th>" +
            "<th>Working</th>" +
            "<th>CPU</th>" +
            "<th>Address</th></tr>";
            String instances = "";
            for(WorkerInstance w : scaler.getWorkers()){
                instances += "<tr><th>" +  w.getId() + "</th>" + 
                "<th>" +  w.getStatus() + "</th>" + 
                "<th>" +  w.getProgress() + "</th>" + 
                "<th>" +  w.working() + "</th>" + 
                "<th>" +  w.getCPU() + "</th>" +
                "<th>" + " <a href=\"http://" + w.getAddress() + ":8000/health\">" + 
                w.getAddress() + "</a></th></tr>";
            }
            
            String response = "<html><head><title>load balancer</title>"+ style +"<head>" +
            "<body><h3>Load Balancer Status</h3>" +
            "<table>" + instanceHeader + instances +"</table>" +
            "<hr>" +
            "<table>" + configHeader + configsStr +"</table>" +
            "</body>";
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
