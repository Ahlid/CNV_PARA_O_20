package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import org.apache.log4j.Logger;

import java.util.*;

import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;

import javax.swing.text.StyledEditorKit;


public class Scaler extends Thread {
    final static Logger logger = Logger.getLogger(Scaler.class);
    private final static Integer CLEANUP = 3;
    private final static Double CPU_THRESHOLD = 60.0;
    private final static Integer SIZE_THRESHOLD = 500;

    private static final String METRICS_TOPIC = "Metrics";

    private Balancer balancer = null;
    private boolean running = true;
    private Messenger messenger = null;
    private AWS aws = null;
    private Integer cleanCounter = 0;
    private Double cpu = 0.0;
    private ArrayList<WorkerInstance> workers;
    private LinkedHashMap<String, String> configs = new LinkedHashMap<>();


    TimerTask sayHello = new TimerTask() {
        @Override
        public void run() {
            try {
                ping();
            } catch (Exception e) {
                logger.error("Error checking if scaling is needed..." + e.getMessage());
            }
        }
    };


    /**
     * Default constructor, initialize AWS and if no instances are running, start one.
     */

    public Scaler() {
        try {
            logger.info("Initializing Scaler...");
            // New AWS connection
            aws = new AWS();
            // Setup credentials
            aws.init();
            // New messenger
            messenger = Messenger.getInstance();
            // Setup db
            messenger.setup();
            // Setup AMI Name
            configs = messenger.fetchConfig();

            aws.setupInstances(configs.get("AMI_Name"));
            workers = aws.getInstances();

            resetPool();
            logger.info("Starting with " + workers.size() + " workers.");
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal(getDefaultUncaughtExceptionHandler().toString());
        }
    }

    public void ping() {

        cpu = 0.0;
        Boolean createInstance = false;
        try {
            if (cleanCounter == CLEANUP) {
                cleanCounter = 0;
                syncWorkers();
                // TODO we need to remove dead instances from dynamoDB
                workers = aws.getInstances();
            }
            cleanCounter++;

            // THIS SHOULD NOT BE DONE EVERY TIME
            //workers = aws.getInstances();

            if (workers.size() < 1) {
                createInstance = true;
            }

            // TODO we need to check if we need new workers
            // or to delete unused workers
            for (WorkerInstance w : workers) {

                if (w.getJobs() >= SIZE_THRESHOLD) {
                    createInstance = true;
                }
                List<String> progress = messenger.getProgress(w.getId());
                logger.info("Progress: " + progress.toString());
                if (progress != null && progress.size() == 3) {
                    if (progress.get(2).equals("false")) {
                        w.setProgress(Double.valueOf(progress.get(0)), Integer.valueOf(progress.get(1)));
                        w.setWork(true);
                    } else {
                        w.setProgress(Double.valueOf(progress.get(0)), Integer.valueOf(progress.get(1)));
                        w.setWork(false);
                    }
                    logger.info(progress.get(0) + " - " + progress.get(1) + " - " + w.getStatus() + " - " + progress.get(2));
                    if (w.getStatus().equals("running")) {
                        cpu += w.getCPU();
                    }
                }
            }
            logger.info("Create instance?: " + createInstance + " | Threshold: " + (Double.valueOf(cpu) / Double.valueOf(workers.size()) > CPU_THRESHOLD));
            if (createInstance || (Double.valueOf(cpu) / Double.valueOf(workers.size()) > CPU_THRESHOLD)) {
                startWorker();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error retreiving workers:" + e.getMessage());
        }
        for (WorkerInstance w : workers) {
            logger.info("Id: " + w.getId() + " | State: " + w.getStatus() + " | CPU: " + w.getCPU().toString() + "| Working: " + w.working());
            messenger.putMessage(w);
        }
    }

    public List<WorkerInstance> syncWorkers() {
        LinkedHashMap<String, Map<String, String>> workerData = messenger.getWorkersTable();
        List<WorkerInstance> w = new ArrayList<>();
        logger.info("sync data: " + workerData.toString());

        for (WorkerInstance wo : workers) {
            logger.info("exists:" + workerData.containsKey(wo.getId()));
        }

        // for (Map.Entry<String, Map<String,String>> entry : workerData.entrySet()) {
        //     String key = entry.getKey();
        //     Map<String,String> value = entry.getValue();
        //     logger.info("id: " + key + " value: " + value.get("status"));
        //     logger.info("exists: " + workers.contains(key));
        //     //messenger.endWorker(key);
        // }
        return w;
    }

    public List<WorkerInstance> getWorkers() {
        return this.workers;
    }

    public ArrayList<WorkerInstance> getInstances() {
        try {
            return aws.getInstances();
        } catch (Exception e) {
            return null;
        }
    }

    public void startWorker() {
        WorkerInstance worker = aws.createInstance();
        workers.add(worker);
        // instances start with and empty address
        // dynamodb doesnt support empty
        worker.setAddress("empty");
        messenger.putMessage(worker);
        balancer.addWorkerBalancer(worker);
    }

    public void terminateWorker(WorkerInstance worker) {
        balancer.removeWorkerBalancer(worker);
        aws.terminateInstance(worker.getId());
    }

    public synchronized void resetPool() {

        LinkedHashMap<String, Map<String, String>> workerData = messenger.getWorkersTable();
        for (Map.Entry<String, Map<String, String>> entry : workerData.entrySet()) {
            String key = entry.getKey();
            Map<String, String> value = entry.getValue();
            logger.info("key: " + key + " value: " + value.get("status"));
            messenger.endWorker(key);
        }

        for (WorkerInstance w : workers) {
            if (!w.getStatus().equals("running")) {
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

    public void setState(Boolean state) {
        this.running = state;
    }

    public void setupConfig(LinkedHashMap<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            messenger.changeConfig(name, value);
            configs = messenger.fetchConfig();
            aws.setupInstances(configs.get("AMI_Name"));
        }
    }

    public LinkedHashMap<String, String> getConfigs() {
        return configs;
    }


}
