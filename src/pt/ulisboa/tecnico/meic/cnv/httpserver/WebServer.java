package pt.ulisboa.tecnico.meic.cnv.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.*;

public class WebServer {

    private static final int port = 8000;
    private static final List<Long> threads = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.createContext(Context.TEST, new MyHandler());
        server.createContext(Context.INDEX, new MyHandler());
        server.createContext(Context.HEALTH, new MyHandler());
        server.createContext(Context.MAZERUN, new MyMazeRunnerHandler());
        server.setExecutor(null); // creates a default executor
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
            String timeStamp = String.valueOf(time.getTime());

            // Get Thread Id and add it to Threads so we can keep track of threads running.
            Long threadId = Thread.currentThread().getId();
            threads.add(threadId);

            try{
                //Maze mazerunner = new Maze.Main(3,9,28,39,50,"astar","Maze50.maze","Maze50.html");
                //Maze mazerun = null;

            }
            catch(Exception e){
                System.out.println(e.toString());
            }

            //t.sendResponseHeaders(200, response.length());
            //OutputStream os = t.getResponseBody();
            //os.write(response.getBytes());
            //os.close();
        }
    }

}
