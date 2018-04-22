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

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.*;

public class WebServer {

    private static final int PORT = 8000;
    private static final int responseCode_OK = 200;
    public static String ROOT_FOLDER = "/home/ec2-user/web/";
    private static final Set<Long> threads = new HashSet<>();
    public static HashMap<Long, Object> requestParams = new HashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.createContext(Context.TEST, new MyHandler());
        server.createContext(Context.INDEX, new MyHandler());
        server.createContext(Context.HEALTH, new MyHandler());
        server.createContext(Context.MAZERUN, new MyMazeRunnerHandler());
        server.createContext(Context.OUTPUT, new MyOutputHandler());
        server.setExecutor(executor);
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String response = "This was the query:" + t.getRequestURI().getQuery()
                    + "##";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class MyMazeRunnerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

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
            List<String> paramsMain = new ArrayList<>(params.values());
            String mazeNameIn = paramsMain.remove(0);
            paramsMain.add(mazeNameIn);

            String mazeNameOut = "maze" + timestamp + ".html";
            paramsMain.add(mazeNameOut);

            String response;

            try {

                String[] paramsMainArray = paramsMain.toArray(new String[paramsMain.size()]);
                Main.main(paramsMainArray);

                response = "<html><title>maze runner </title><br><body>" +
                    "<hr><form action=\"/mzrun.html\">" +
                    "<b>Model Filename:</b> <input name=\"m\" type=\"text\"  ><br><br>" +
                    "x0: <input name=\"x0\" type=\"text\">" +
                    "y0: <input name=\"y0\" type=\"text\" ><br>" +
                    "x1: <input name=\"x1\" type=\"text\">" +
                    "y1: <input name=\"y1\" type=\"text\" ><br>" +
                    "Velocity: <input name=\"v\" type=\"text\"><br>" +
                    "Strategy: <input name=\"s\" type=\"text\"><br>" +
                    "<input type=\"submit\" value=\"Submit\">" +
                    "</form>" +
                    "<a href=/output?f=" + mazeNameOut + ">Output</a><hr>" +
                    "<iframe align=center width=600 height=400 src=/output?f=" + mazeNameOut +"></iframe>" +
                    "</body></html>";
            } catch(Exception e) {
                System.out.println(e.toString());
                response = "<html><title>maze runner </title><br><body>" +
                ""+ e.toString() + "<hr>" +
                "</body></html>";
            }

            t.sendResponseHeaders(responseCode_OK, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

            // Remove threadId from data structures
            threads.remove(threadId);
            requestParams.remove(threadId);

        }
    }

    static class MyOutputHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

            String query = he.getRequestURI().getQuery();
            String f = query.split("f=")[1];

            Headers headers = he.getResponseHeaders();
            headers.add("Content-Type", "text/html");

            File file = new File(f);
            byte[] bytes = new byte[(int) file.length()];
            System.out.println(file.getAbsolutePath());
            System.out.println("length:" + file.length());

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes, 0, bytes.length);
            bufferedInputStream.close();

            he.sendResponseHeaders(responseCode_OK, file.length());
            OutputStream outputStream = he.getResponseBody();
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
        }
    }

}
