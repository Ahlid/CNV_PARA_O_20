package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import pt.ulisboa.tecnico.meic.cnv.httpserver.WebServer;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MetricsInstrumentation {

    private static long BB_SIZE = 1000000;

    /**
     * The hashMap for the metrics on each thread execution
     */
    private static HashMap<Long, Metrics> threadMetrics = new HashMap<>();


    /**
     * Gets the metrics object for the current running thread
     *
     * @return - metrics object
     */
    private static Metrics getMetricsForThread() {
        // Get the thread id
        Long threadId = Thread.currentThread().getId();

        // Create a metrics object for current thread if it doesn't exist
        if (!threadMetrics.containsKey(threadId))
            threadMetrics.put(threadId, new Metrics());

        Metrics metrics = threadMetrics.get(threadId);
        return metrics;
    }

    /**
     * Method to be called when a new routine starts
     *
     * @param name - the name of the method
     */
    public static synchronized void dynMethodCount(String name) {

        Metrics metrics = getMetricsForThread();
        metrics.setDyn_method_count(metrics.getDyn_method_count() + 1);
    }

    /**
     * Method to be called when a basic block is executed
     *
     * @param incr - number of instructions inside basic block
     */
    public static synchronized void dynBasicBlockCount(int incr) {
        Metrics metrics = getMetricsForThread();
        metrics.setDyn_instr_count(metrics.getDyn_instr_count() + incr);
        metrics.setDyn_bb_count(metrics.getDyn_bb_count() + 1);
        // Notifier
        if( metrics.getDyn_bb_count() % BB_SIZE == 0 ){
            WebServer.updateMetrics(metrics.getDyn_instr_count(), metrics.getDyn_bb_count(), false);
        }
    }

    /**
     * Method to print the metrics
     */
    public static synchronized void printDynamic(String foo) {
        //System.out.println(foo);
        Metrics metrics = getMetricsForThread();
        metrics.print();
    }

    public static synchronized void endOfThreadExecution(String foo) {
        Long threadId = Thread.currentThread().getId();
        Metrics metrics = getMetricsForThread();
        // Notify about end of execution
        WebServer.updateMetrics(metrics.getDyn_instr_count(), metrics.getDyn_bb_count(), true);
        threadMetrics.put(threadId, new Metrics());
    }

    public static synchronized void setRequest(String foo) {
        //todo: get the request payload
        Long threadId = Thread.currentThread().getId();
        System.out.println(threadId);
        Metrics metrics = getMetricsForThread();
        // Notify about end of execution
        WebServer.updateMetrics((long) 0, (long) 0, false);

        metrics.setThreadID((int) (long) threadId);
        metrics.setRequestParams((LinkedHashMap) WebServer.requestParams.get(threadId));

    }

}
