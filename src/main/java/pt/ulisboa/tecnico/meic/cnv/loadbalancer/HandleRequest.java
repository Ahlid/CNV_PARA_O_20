package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.io.OutputStream;
import java.net.URL;

public class HandleRequest implements HttpHandler {

    final static Logger logger = Logger.getLogger(HandleRequest.class);
    static final int responseCode_OK = 200;
    static final int retryRequestTime = 5000;

    private Scaler scaler;
    private Balancer balancer;

    private static String TIMESTAMP = System.currentTimeMillis() + "";
    private static int LAST_JOB_ID = 0;

    public HandleRequest(Scaler scaler, Balancer balancer) {
        super();
        this.scaler = scaler;
        this.balancer = balancer;
        logger.info("Setting up handler");
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("GET")) {

            String jobId = TIMESTAMP + (++LAST_JOB_ID);

            String query = t.getRequestURI().getQuery();
            String response = "";
            LinkedHashMap<String, String> params = new LinkedHashMap<>();

            if (query != null) {

                int cost = CostFunction.calculateCost(query);
                System.out.println("MY COST WASSSSS");
                System.out.println(cost);

                Job thisJob = new Job(jobId, cost);
                JobsPool.getInstance().addJob(thisJob);

                while (response == "") {

                    WorkerInstance worker = balancer.getInstance();
                    thisJob.setWorkerInstance(worker);



                    try {
                        String link = "http://" + worker.getAddress() + ":8000" + t.getRequestURI().toString() + "&jobId=" + thisJob.getId();
                        logger.info("Sending request to: " + worker.getId());
                        URL url = new URL(link);

                        HttpURLConnection wc = (HttpURLConnection) url.openConnection();


                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(wc.getResponseCode() / 100 == 2 ? wc.getInputStream() : wc.getErrorStream()));

                        for (String line; (line = in.readLine()) != null; response += line + "\n") ;
                        in.close();
                    } catch (Exception e) {
                        thisJob.setWorkerInstance(null);
                        logger.error("error in request: " + e.getMessage());
                        logger.error("need to do something and re-do request");
                    }
                    Thread.sleep(retryRequestTime);
                }

                JobsPool.getInstance().endJob(thisJob);

                if (response != "") {
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

}
