package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import org.apache.log4j.Logger;

import java.util.*;

import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;

import javax.swing.text.StyledEditorKit;


public class Scaler extends Thread{
    final static Logger logger = Logger.getLogger(Scaler.class);
    private final static Integer CLEANUP = 4;
    private final static Double CPU_THRESHOLD = 50.0;
    private final static Integer SIZE_THRESHOLD = 500;

    private static final String METRICS_TOPIC = "Metrics";

    private Balancer balancer = null;
    private boolean running = true;
    private Messenger messenger = null;
    private Integer cleanCounter = 0;
    private Double cpu = 0.0;
    ArrayList<WorkerInstance> workers;

    TimerTask sayHello = new TimerTask() {
        @Override
        public void run() {
            try {
                ping();
            }
            catch (Exception e) {
                System.out.println("something went wrong at: " + e.getMessage());
            }
        }
    };


/**
 *  Default constructor, initialize AWS and if no instances are running, start one.
 */

    public Scaler(){ // throws Exception {
        try {
            logger.info("Initializing Scaler...");
            AWS.init();
            messenger = new Messenger();
            workers = AWS.getInstances();
            resetPool();
            //messenger.deleteTable(METRICS_TOPIC);
            messenger.setup();
            if (workers.size() == 0) {
                logger.info("No workers detected, starting one, waiting 1min to pool aws...");
                //startWorker();
                AWS.createInstance();
                Thread.sleep(60000);
                workers = AWS.getInstances();
            }
            logger.info("Starting with " + workers.size() + " workers.");
        }
        catch (Exception e) {
            logger.fatal(getDefaultUncaughtExceptionHandler().toString());
        }
    }

    public void ping(){
        if(cleanCounter++ == CLEANUP){ cleanCounter = 0; }
        cpu = 0.0;
        Boolean createInstance = false;
        try {
            workers = AWS.getInstances();

            if (workers.size() < 1){ createInstance = true;}

            for (WorkerInstance w : workers){

                if (w.getSize() >= SIZE_THRESHOLD) {createInstance = true;}
                List<String> progress = messenger.getProgress(w.getId());
                System.out.println(w.getSize());
                if (progress != null && progress.size() ==3) {
                    if (progress.get(2).equals("false")) {
                        w.setProgress(Double.valueOf(progress.get(0)), Integer.valueOf(progress.get(1)));
                        System.out.println("not finish" + w.getId());
                        w.setWork(true);
                    }
                    else {
                        w.setProgress(Double.valueOf(progress.get(0)), Integer.valueOf(progress.get(1)));
                        w.setWork(false);
                        System.out.println("finish" + w.getId());
                    }
                    //System.out.println(progress.get(0) + " - " + progress.get(1) + " - " + w.getStatus() + " - " + progress.get(2));
                    if (w.getStatus().equals("running")) {
                        //System.out.println("it's running cpu use is " + w.getCPU());
                        cpu += w.getCPU();
                    }
                }
            }
            System.out.println("create: " + createInstance + " threshold: " + (Double.valueOf(cpu)/Double.valueOf(workers.size())>CPU_THRESHOLD));
            //if(createInstance || (Double.valueOf(cpu)/Double.valueOf(workers.size())>CPU_THRESHOLD)){ startWorker(); }

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("oops... old workers:" + e.getMessage());
        }
        System.out.println("workers: " + workers.size());
        for(WorkerInstance w : workers){
            System.out.println("id: " + w.getId() + " > state: " + w.getStatus() + " cpu: " + w.getCPU().toString());
            messenger.putMessage(w);
        }
    }

    public List<WorkerInstance> getWorkers(){
        return this.workers;
    }

    public void startWorker(){
        //WorkerInstance worker =
        //        AWS.createInstance();
        //workers.add(worker);
        //messenger.putMessage(worker);
        //balancer.addWorkerBalancer(worker);
    }

    public void terminateWorker(WorkerInstance worker){
        balancer.removeWorkerBalancer(worker);
        AWS.terminateInstance(worker.getId());
    }

    public synchronized void resetPool(){
        for (WorkerInstance w : workers){
            if(!w.getStatus().equals("running")) {
                terminateWorker(w);
                w.setStatus("dead");
                messenger.putMessage(w);
            }
        }
        // clean and delete everything
    }

    /**
     * Takes a balancer as argument to be able
     * to notify it when a new instance is running
     */
    public Scaler(Balancer balancer) throws Exception {
        this();
        this.balancer = balancer;
    }

    public void setState(Boolean state){
        this.running = state;
    }

}
