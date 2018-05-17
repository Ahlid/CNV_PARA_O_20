package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;

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
        proxiedServer.createContext("/lb", new MyHandler());
        proxiedServer.setExecutor(Executors.newCachedThreadPool());
        proxiedServer.start();

        logger.info("Proxy started.");

        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(scaler.sayHello, 0, 30000);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String instanceHeader = "<tr><th>ID</th>" + 
                                    "<th>Status</th>" + 
                                    "<th>CPU</th>" +
                                    "<th>Address</th></tr>";
            String instances = "";
            for(WorkerInstance w : scaler.getInstances()){
                instances += "<tr><th>" +  w.getId() + "</th>" + 
                             "<th>" +  w.getStatus() + "</th>" + 
                             "<th>" +  w.getCPU() + "</th>" +
                             "<th>" + " <a href=\"http://" + w.getAddress() + ":8000/health\">" + 
                             w.getAddress() + "</a></th></tr>";
            }

            String response = "<html><head><title>load balancer</title><head>" +
                              "<body><h3>Load Balancer Status</h3>" +
                              "<table>" + instanceHeader + instances +"</table>" +
                              "</body>";
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
