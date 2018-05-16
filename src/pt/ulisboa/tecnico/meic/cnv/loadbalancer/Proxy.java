package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.Executors;

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
            System.out.println("oops at proxy: " + e.getMessage());
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
        proxiedServer.setExecutor(Executors.newCachedThreadPool());
        proxiedServer.start();

        logger.info("Proxy started.");

        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(scaler.sayHello, 0, 30000);
    }
}
