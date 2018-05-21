package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import com.amazonaws.services.dynamodbv2.xspec.M;
import org.apache.log4j.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.net.SocketTimeoutException;

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
    public final static Long longRequestLimit = 5000000000L;
    public final static Long shortRequestLimit = 20000000L;  
    
    private static final String METRICS_TOPIC = "Metrics";
    
    private Balancer balancer = null;
    private boolean running = true;
    private Messenger messenger = null;
    private AWS aws = null;
    private Integer cleanCounter = 0;
    private Double cpu = 0.0;
    private Integer minWorkers = 0;
    private Integer maxWorkers = 0;
    private Integer timeLimit = 240000;
    private Integer lastCreation = 0;

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

            minWorkers = Integer.parseInt(configs.get("MIN_WORKERS"));
            maxWorkers = Integer.parseInt(configs.get("MAX_WORKERS"));

            aws.setWorkerAmiId(configs.get("AMI_Name"));
            aws.setupInstances();
            
            workers = aws.getInstances();
            
            resetPool();
            logger.info("Starting with " + workers.size() + " workers.");
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal(getDefaultUncaughtExceptionHandler().toString());
        }
    }
    
    public void ping() {
        synchronized (this.workers) {
        
        Boolean createInstance = false;
        Boolean destroyInstance = false;
        double sizeBB = 0;
        cpu = 0.0;

        try {
            // check every timer*cleanup seconds if is alive
            if (cleanCounter == CLEANUP) {
                cleanCounter = 0;
                updateAliveWorkers();
            }
            cleanCounter++;
            syncJobs();
            
            if (workers.size() < minWorkers) {
                
            }

            for (WorkerInstance w : this.workers) {
                sizeBB += w.getBBtoBeProcessed();   
                cpu += w.getCPU();
            }

            if(Double.valueOf(cpu)/Double.valueOf(workers.size())>CPU_THRESHOLD){ 
                createInstance = true; 
            }

            if (sizeBB >= 3*shortRequestLimit+longRequestLimit || sizeBB >= shortRequestLimit+2*longRequestLimit){
                createInstance = true;
            }

            lessRelevant();
            
            // destroy instances ??
            if (workers.size() > maxWorkers){
                WorkerInstance workerToDestroy = lessRelevant();
                if (workerToDestroy.getJobs() == 0){
                    terminateWorker(workerToDestroy);
                }
                else{
                    workerToDestroy.setAcceptingRequests(false);
                }
            }

            if(createInstance &&((int)System.currentTimeMillis() - lastCreation) > timeLimit || lastCreation == 0 && workers.size() < minWorkers) {
                startWorker();
                lastCreation = (int) System.currentTimeMillis();
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
}

    public WorkerInstance lessRelevant(){
        WorkerInstance worker = workers.get(0);
        
        for (WorkerInstance w : workers) {
            logger.info(w.getJobs());
            if (w.getJobsSize() < w.getJobsSize()){
                worker = w;
            }
            
        }

        logger.info(worker.getId());

        return worker;
    }
    
    public void updateAliveWorkers(){
        List<WorkerInstance> wrks = getWorkers();
        HttpURLConnection conn = null;

        
        
        for (WorkerInstance w : wrks) {
            logger.info(w.getId() + ":" + w.getStatus());
            if(w.getStatus().equals("running")){
                try{
                    URL url = new URL("http://" + w.getAddress() + ":8000/health" );
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.connect();
                }
                catch(SocketTimeoutException e){
                    terminateWorker(w);
                    //logger.error("Exception:" + e.getMessage());
                }
                catch (Exception e){
                    logger.error("Exception:" + e.getMessage());
                }
            }   
        }
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
                    
                    //logger.info(JobsPool.getInstance().getJobs().toString());
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
                workers.remove(worker);
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
                }
                
                configs = messenger.fetchConfig();
                aws.setWorkerAmiId(configs.get("AMI_Name"));
                aws.setupInstances();
                minWorkers = Integer.parseInt(configs.get("MIN_WORKERS"));
                maxWorkers = Integer.parseInt(configs.get("MAX_WORKERS"));
                
                //aws.setupInstanceRequest(1, 1);
            }
            
            public LinkedHashMap<String, String> getConfigs() {
                return configs;
            }
            
            


    public WorkerInstance getBestWorker() {
        synchronized (this.workers) {

            WorkerInstance chosenWorker = null;

            for (WorkerInstance w : this.workers) {

                
                logger.info("id: " + w.getId() + " BB: " + w.getBBtoBeProcessed());

                if (!w.isAcceptingRequests() || !w.getStatus().equals("running"))
                    continue;

                if (chosenWorker == null) {
                    chosenWorker = w;
                } else if (chosenWorker.getBBtoBeProcessed() > w.getBBtoBeProcessed()) {
                    chosenWorker = w;
                }
            }
            logger.info("Faster worker: " + chosenWorker.getId());
            return chosenWorker;
        }
    }

}
