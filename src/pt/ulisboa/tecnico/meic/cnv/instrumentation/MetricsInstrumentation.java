package pt.ulisboa.tecnico.meic.cnv.instrumentation;

import pt.ulisboa.tecnico.meic.cnv.httpserver.WebServer;

import java.util.HashMap;

public class MetricsInstrumentation {

    /**
     * The hashMap for the metrics on each thread execution
     */
    private static HashMap<Long, Metrics> threadMetrics = new HashMap<>();


    /**
     * Gets the metrics object for the specific thread
     *
     * @return
     */
    private static Metrics getMetricForThread() {
        //get the thread id
        Long threadId = Thread.currentThread().getId();

        //create the metrics counter if not exists
        if (!threadMetrics.containsKey(threadId))
            threadMetrics.put(threadId, new Metrics());

        Metrics metrics = threadMetrics.get(threadId);
        return metrics;
    }

    /**
     * Me method to be called when a new routine starts
     *
     * @param name - the name of the method
     */
    public static synchronized void dynMethodCount(String name) {

        Metrics metrics = getMetricForThread();
        metrics.addMethodCount(name);
        metrics.setDyn_method_count(metrics.getDyn_method_count() + 1);
    }


    /**
     * Method to be called when an instruction is executed
     *
     * @param incr - ???
     */
    public static synchronized void dynInstrCount(int incr) {
        Metrics metrics = getMetricForThread();
        metrics.setDyn_instr_count(metrics.getDyn_instr_count() + 1);
        metrics.setDyn_bb_count(metrics.getDyn_bb_count() + 1);
    }

    /**
     * Method to print the metrics
     */
    public static synchronized void printDynamic(String foo) {
        System.out.println(foo);
        Metrics metrics = getMetricForThread();
        metrics.print();
    }

    public static synchronized void endOfThreadExecution(String foo) {
        Long threadId = Thread.currentThread().getId();
        threadMetrics.put(threadId, new Metrics());
    }

    public static synchronized void setRequest(String foo) {
        //todo: get the request payload
        Long threadId = Thread.currentThread().getId();
        Metrics metrics = getMetricForThread();
        metrics.setRequestParams(WebServer.requestMap.get(threadId));

    }

}
