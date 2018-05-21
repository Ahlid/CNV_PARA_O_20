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
import java.util.concurrent.atomic.AtomicLong;
import java.io.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;
import com.amazonaws.util.EC2MetadataUtils;

import com.amazonaws.util.EC2MetadataUtils;

import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.*;

public class WebServer {
    final static Logger logger = Logger.getLogger(WebServer.class);

    private static final int PORT = 8000;
    private static final int responseCode_OK = 200;
    public static String HOME_FOLDER = "/home/ec2-user/";
    private static final Set<Long> threads = new HashSet<>();
    public static HashMap<Long, Object> requestParams = new HashMap<>();
    public static HashMap<Long, String> pureRequest = new HashMap<>();
    private static HashMap<Long, String> requestId = new HashMap<>();
    private static AtomicLong highestRequestId = new AtomicLong();
    private static HashMap<Long, String> jobsId = new HashMap<>();
    private static Messenger messenger = null;
    private static String instanceId = null;
    private static String endpoint = null;


    public static void main(String[] args) throws Exception {

        // LOCAL TESTING
        //instanceId = "i-222563b5019e4f222";
        //endpoint = "localhost";

        // Read worker machine details at startup
        // instance public address
        instanceId = EC2MetadataUtils.getInstanceId();
        logger.info("Instance Id: " + instanceId);

        endpoint = EC2MetadataUtils.getData("/latest/meta-data/public-hostname") + ":" + PORT;

        logger.info("Public endpoint: " + endpoint);

        // Create new Messenger, to place information at Dynamo
        messenger = Messenger.getInstance();
        // send machine data to dynamo
        // update dynamo with info about worker
        messenger.newWorker(instanceId, endpoint);
        // status , working
        updateWorker("running", false);


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

            logger.info("Thread Id: " + threadId);
            if (!res) {
                logger.error("Separate requests have the same threadId=" + threadId);
                throw new RuntimeException("This should not happen!");
            }

            String query = t.getRequestURI().getQuery();
            String[] paramList = query.split("&");
            String jobId = "";

            for (String param : paramList) {
                String paramName = param.split("=")[0];
                String paramValue = param.split("=")[1];
                if (paramName.equals("jobId")) {
                    jobId = paramValue;
                }
            }

            // Create unique requestId for current request
            Long newRequestId = highestRequestId.getAndIncrement();
            requestId.put(threadId, jobId);
            logger.info("Request Id: " + requestId);

            // Keep track of URL parameters for each thread
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            requestParams.put(threadId, params);

            // Process URL query string

            // Store query string parameters in LinkedHashMap(preserving insertion order)
            for (String param : paramList) {

                String paramName = param.split("=")[0];
                if (paramName.equals("jobId")) {
                    continue;
                }
                String paramValue = param.split("=")[1];
                params.put(paramName, paramValue);
            }

            pureRequest.put(threadId, params.toString());

            logger.info("Request params: " + params);

            // Main class expects parameters in order <x0,y0,x1,y1,v,s,m,mazeNameOut>

            String mazeNameOut = "maze" + timestamp + ".html";
            params.put("out", mazeNameOut);

            // Notify that this worker is working
            updateWorker("running", true);

            String response;

            try {

                String[] paramsArray = {params.get("x0"), params.get("y0"),
                        params.get("x1"), params.get("y1"),
                        params.get("v"), params.get("s"),
                        params.get("m"), params.get("out")};
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

                logger.info("Finished processing request: " + newRequestId);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.toString());
                response = "<html><title>maze runner </title><br><body>" +
                        "" + e.toString() + "<hr>" +
                        "</body></html>";
                t.sendResponseHeaders(responseCode_OK, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

            
            // Notify that this worker is not working
            if (requestId.size()==0){
            updateWorker("running", false);
            }


            // Remove thread from running list
            threads.remove(threadId);
            requestId.remove(threadId);
            requestParams.remove(threadId);

        }
    }


    public static void updateWorker(String status, Boolean working) {
        // instanceId, status, cpu, endpoint, working, jobs
        messenger.workerUpdate(instanceId, status, 0.0, endpoint, working, requestId.size());
    }

    public static HashMap<Long, Object> getRequestParams() {
        return requestParams;
    }

    public static HashMap<Long, String> getPureRequest() {
        return pureRequest;
    }

    public static HashMap<Long, String> getRequestId() {
        return requestId;
    }

    public static AtomicLong getHighestRequestId() {
        return highestRequestId;
    }

    public static String getInstanceId() {
        return instanceId;
    }
}
