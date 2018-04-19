package pt.ulisboa.tecnico.meic.cnv.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
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
    private static final List<Long> threads = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.createContext(Context.TEST, new MyHandler());
        server.createContext(Context.INDEX, new MyHandler());
        server.createContext(Context.HEALTH, new MyHandler());
        server.createContext(Context.MAZERUN, new MyMazeRunnerHandler());
        server.createContext(Context.OUTPUT, new MyOutputHandler());
        server.setExecutor(executor); // creates a default executor
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
            String query = t.getRequestURI().getQuery();
            String[] values = query.split("&");
            String f = values[0].split("m=")[1],
                    x0 = values[1].split("x0=")[1],
                    y0 = values[2].split("y0=")[1],
                    x1 = values[3].split("x1=")[1],
                    y1 = values[4].split("y1=")[1],
                    v = values[5].split("v=")[1],
                    s = values[6].split("s=")[1];

            // Get timestamp to generate a file with a name without collisions
            Timestamp time = new Timestamp(System.currentTimeMillis());
            String timestamp = String.valueOf(time.getTime());

            // Get Thread Id and add it to Threads so we can keep track of threads running.
            Long threadId = Thread.currentThread().getId();
            threads.add(threadId);
            System.out.println("boing");
            System.out.println(threadId);
            
            String mazeNameOut = "maze" + timestamp + ".html";

            try{
               
                //time = new Timestamp(System.currentTimeMillis());
                //System.out.println(String.valueOf(time.getTime()));
                Main.main(new String[] {x0,y0,x1,y1,v,s,f,mazeNameOut});
                //time = new Timestamp(System.currentTimeMillis());
                //System.out.println(String.valueOf(time.getTime()));
            }
            catch(Exception e){
                System.out.println(e.toString());
            }

            String response = "<html><title>maze runner </title><br><body>" +
                    "<a href=/output?f=" + mazeNameOut + ">Output</a><hr>" +
                    "<iframe align=center width=600 height=400 src=/output?f=" + mazeNameOut +"></iframe>" +
                    "</body></html>";
            t.sendResponseHeaders(responseCode_OK, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class MyOutputHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange he) throws IOException {

            String query =  he.getRequestURI().getQuery();
            String f = query.split("f=")[1];

            Headers headers = he.getResponseHeaders();
            headers.add("Content-Type", "text/html");

            File file = new File ( f );
            byte[] bytes = new byte [(int)file.length()];
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
