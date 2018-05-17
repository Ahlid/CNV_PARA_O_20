package pt.ulisboa.tecnico.meic.cnv.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.sql.Timestamp;
import java.io.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.*;

public class WebServer {

    private static final int PORT = 8000;
    private static final int responseCode_OK = 200;
    public static String HOME_FOLDER = "/home/ec2-user/";
    private static final Set<Long> threads = new HashSet<>();
    public static HashMap<Long, Object> requestParams = new HashMap<>();
    private static Messenger messenger = null;
    private static String amiId = null;

    public static void main(String[] args) throws Exception {

        // read machine details from ~/id.txt - fetched when machine is booting
        List<String> id = new ArrayList<>();
        id =getMachineDetails();
        // instance id
        String amiId = id.get(0);
        // instance public address
        String address = id.get(1) + ":" + PORT;

        // create new messenger, to put information at dynamo
        messenger = new Messenger();
        // send machine data to dynamo
        messenger.newWorker(amiId, address);
        messenger.workerUpdate(amiId,"0",0,true);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.createContext(Context.INDEX, new MyHandler());
        server.createContext(Context.HEALTH, new MyHandler());
        server.createContext(Context.MAZERUN, new MyMazeRunnerHandler());
        server.setExecutor(executor);
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String response = "I'm alive!";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class MyMazeRunnerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            // Work Size
            Integer size = 0;

            // Get timestamp to generate a file with a name without collisions
            Timestamp time = new Timestamp(System.currentTimeMillis());
            String timestamp = String.valueOf(time.getTime());

            // Get Thread Id and add it to Threads so we can keep track of running threads 
            Long threadId = Thread.currentThread().getId();
            boolean res = threads.add(threadId);

            System.out.println();
            System.out.println("Thread Id: " + threadId);
            if (!res) {
                System.out.println("Error: Separate requests have the same threadId=" + threadId);
                throw new RuntimeException("This should not happen!");
            }

            // Keep track of URL parameters for each thread
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            requestParams.put(threadId, params);

            // Process URL query string
            String query = t.getRequestURI().getQuery();
            String[] paramList = query.split("&");

            // Store query string parameters in LinkedHashMap(preserving insertion order)
            for (String param : paramList) {
                String paramName = param.split("=")[0];
                String paramValue = param.split("=")[1];
                params.put(paramName, paramValue);
            }

            System.out.println("Request params: " + params);

            // Main class expects parameters in order <x0,y0,x1,y1,v,s,m,mazeNameOut>

            String mazeNameOut = "maze" + timestamp + ".html";
            params.put("out", mazeNameOut);

            String response;

            try {

                String[] paramsArray = {params.get("x0"), params.get("y0"),
                                        params.get("x1"),params.get("y1"),
                                        params.get("v"),params.get("s"),
                                        params.get("m"),params.get("out")};
                Main.main(paramsArray);

                File file = new File(mazeNameOut);
                byte[] bytes = new byte[(int) file.length()];
    
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(bytes, 0, bytes.length);
                bufferedInputStream.close();
    
                t.sendResponseHeaders(responseCode_OK, file.length());
                OutputStream outputStream = t.getResponseBody();
                outputStream.write(bytes, 0, bytes.length);
                outputStream.close();

                System.out.println("Finished!");

            } catch(Exception e) {
                System.out.println(e.toString());
                response = "<html><title>maze runner </title><br><body>" +
                ""+ e.toString() + "<hr>" +
                "</body></html>";
                t.sendResponseHeaders(responseCode_OK, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }


            // Remove thread from running list
            threads.remove(Thread.currentThread().getId());
            requestParams.remove(threadId);

        }
    }

    public static List<String> getMachineDetails(){
        List<String> details = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(HOME_FOLDER +"id.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    details.add(line);
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                //String everything = sb.toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return details;
    }

}
