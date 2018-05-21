package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import com.amazonaws.services.dynamodbv2.xspec.M;
import org.apache.log4j.Logger;

import java.util.*;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupResult;
import com.amazonaws.services.autoscaling.model.LaunchTemplateSpecification;

import pt.ulisboa.tecnico.meic.cnv.storage.Messenger;

import javax.swing.text.StyledEditorKit;


public class Scaler extends Thread {
    final static Logger logger = Logger.getLogger(Scaler.class);
    private final static Integer CLEANUP = 3;
    private final static Double CPU_THRESHOLD = 60.0;
    private final static Integer SIZE_THRESHOLD = 500;
    public final static Long longRequesLimit = 5000000000L;
    public final static Long rapidRequesLimit = 20000000L;

    private static final String METRICS_TOPIC = "Metrics";

    private Balancer balancer = null;
    private boolean running = true;
    private Messenger messenger = null;
    private AWS aws = null;
    private Integer cleanCounter = 0;
    private Double cpu = 0.0;
    private ArrayList<WorkerInstance> workers;
    private LinkedHashMap<String, String> configs = new LinkedHashMap<>();
    private AmazonAutoScaling autoScaler = null;
    private static final String WORKER_TEMPLATE_NAME = "CNV-worker-template";


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
            logger.info(configs);

            // TODO use configs from dynamo

            //logger.info(Integer.parseInt(configs.get("MIN_WORKER")) + ":" + Integer.parseInt(configs.get("MAX_WORKER")));
            aws.setupInstanceRequest(1, 1);
            aws.setWorkerAmiId(configs.get("AMI_Name"));
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
            syncJobs();
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
        // LinkedHashMap<String, Map<String, String>> workerData = messenger.getWorkersTable();
        List<WorkerInstance> w = new ArrayList<>();
        // logger.info("sync data: " + workerData.toString());

        // for (WorkerInstance wo : workers) {
        //     logger.info("exists:" + workerData.containsKey(wo.getId()));
        // }

        // for (Map.Entry<String, Map<String,String>> entry : workerData.entrySet()) {
        //     String key = entry.getKey();
        //     Map<String,String> value = entry.getValue();
        //     logger.info("id: " + key + " value: " + value.get("status"));
        //     logger.info("exists: " + workers.contains(key));
        //     //messenger.endWorker(key);
        // }
        return w;
    }

    public void syncJobs() {

        Messenger m = Messenger.getInstance();

        synchronized (this.workers) {
            for (WorkerInstance w : this.workers) {
                HashMap<String, Long> jobStatus = m.getMetrics(w.getId());

                Set<String> jobIds = jobStatus.keySet();
                Iterator<String> it = jobIds.iterator();

                while (it.hasNext()) {
                    String id = it.next();
                    JobsPool.getInstance().serJobBBWork(id, jobStatus.get(id));
                }

            }

            System.out.println(JobsPool.getInstance().getJobs());
        }


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

        messenger.resetWorkers();

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
            aws.setupInstanceRequest(1, 1);
        }
    }

    public LinkedHashMap<String, String> getConfigs() {
        return configs;
    }

    public WorkerInstance getBestWorker() {
        synchronized (this.workers) {

            WorkerInstance chosenWorker = null;

            for (WorkerInstance w : this.workers) {

                if (!w.isAcceptingRequests() || !w.getStatus().equals("running"))
                    continue;

                if (chosenWorker == null) {
                    chosenWorker = w;
                } else if (chosenWorker.getBBtoBeProcessed() > w.getBBtoBeProcessed()) {
                    chosenWorker = w;
                }
            }

            return chosenWorker;
        }
    }

}
