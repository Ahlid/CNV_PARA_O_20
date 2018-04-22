package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by Dario on 16/05/2017.
 */
public class HandleRequest implements HttpHandler {
    final static Logger logger = Logger.getLogger(HandleRequest.class);

    static final int responseCode_OK = 200;



    final String HTML = "<html><title>maze runner </title><br><body>" +
                        "<hr><form action=\"/mzrun.html\">" +
                        "<b>Model Filename:</b> <input name=\"m\" type=\"text\"  ><br>" +
                        "<b>Start Point</b><br>" +
                        "x0: <input name=\"x0\" type=\"text\">" +
                        "y0: <input name=\"y0\" type=\"text\" ><br>" +
                        "<b>Finish Point </b><br>" +
                        "x1: <input name=\"x1\" type=\"text\">" +
                        "y1: <input name=\"y1\" type=\"text\" ><br>" +
                        "<b>Velocity and Strategy</b><br>" +
                        "Velocity: <input name=\"v\" type=\"text\">" +
                        "Strategy: <input name=\"s\" type=\"text\"><br><br>" +
                        "<input type=\"submit\" value=\"Submit\">" +
                        "</form>" +
                        "</body></html>";

    private Scaler scaler;
    private Balancer balancer;

    public HandleRequest(Scaler scaler, Balancer balancer) {
        super();
        this.scaler = scaler;
        this.balancer = balancer;
        logger.info("Setting up handler");
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("GET")) {
            //logger.info("GET request from " + t.getRequestURI());
            String query =  t.getRequestURI().getQuery();
            String response = null;

            LinkedHashMap<String, String> params = new LinkedHashMap<>();

            //logger.info("Params are: " + query);
            if (query != null) {

                WorkerInstance worker = balancer.getInstance();
                //logger.info("Sending request to " + worker.getAddress() + t.getRequestURI().toString());
                try {
                    response = callWorker(worker.getAddress(), t.getRequestURI().toString(), "12345");
                }
                catch (Exception e){
                    logger.error("error in request: " + e.getMessage());
                    logger.error("need to do something and re-do request");
                }

                if (response != null) {
                    t.sendResponseHeaders(responseCode_OK, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } else {
                    t.sendResponseHeaders(500, 0);
                    t.getResponseBody().close();
                }


            } else {
                t.sendResponseHeaders(responseCode_OK, HTML.length());
                OutputStream os = t.getResponseBody();
                os.write(HTML.getBytes());
                os.close();
            }
        } else {
            logger.warn("Unsupported method");
            t.sendResponseHeaders(405, 0);
            t.getResponseBody().close();
        }
    }

    private String callWorker(String address, String number, String uniqId) throws Exception {
        String url = "http://" + address + ":8000" + number;
        //String url = "http://localhost:8000" + number;
        logger.info("Doing request to: " + url);
        URL worker = new URL(url);
        URLConnection wc = worker.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(wc.getInputStream()));

        String answer = in.readLine();
        in.close();
        return answer;

    }

}
