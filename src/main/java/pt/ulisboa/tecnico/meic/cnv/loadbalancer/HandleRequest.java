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
            String query =  t.getRequestURI().getQuery();
            String response = null;
            LinkedHashMap<String, String> params = new LinkedHashMap<>();

            if (query != null) {

                WorkerInstance worker = balancer.getInstance();
                logger.info("Sending request to " + worker.getAddress() + t.getRequestURI().toString());
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
                String HTML = "null";
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

        String answer = new String();
        for (String line; (line = in.readLine()) != null; answer += line + "\n");
        in.close();
        return answer;

    }

}
