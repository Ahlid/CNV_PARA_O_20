package pt.ulisboa.tecnico.meic.cnv.loadbalancer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class LoadBalancer implements Balancer {
    final static Logger logger = Logger.getLogger(LoadBalancer.class);

    protected List<WorkerInstance> workers;
    protected Scaler scaler;

    public LoadBalancer() throws Exception{
        logger.info("Initializing Balancer...");
        workers = new ArrayList<>();

    }

    public void setScaler(Scaler scaler) { this.scaler = scaler; }

    public WorkerInstance getInstance(){
        Double cpu = 100.0;
        Double progress = 0.0;
        Integer size = 150;
        WorkerInstance faster=null;
        workers = scaler.getWorkers();


        // TODO need to choose the best worker for the job!
        // according to things like metrics and cpu use, and progress done
        for (WorkerInstance w : workers){
            System.out.println("[lb] :id : " + w.getId() + ", cpu use: " + w.getCPU() + ", progress: " + w.getProgress() + ", size: " + w.getSize() + ", working: " + w.working());
            if (!w.working() || (w.getCPU() < cpu) && (w.getProgress() > 0.50) || w.getSize() == 0 ){
                cpu = w.getCPU();
                progress = w.getProgress();
                faster = w;
                System.out.println("hello world!");
            }
            else { return workers.get(0); }
        }

        return workers.get(workers.indexOf(faster));
    }

    @Override
    public synchronized void addWorkerBalancer(WorkerInstance instance) {
        logger.info("Add worker " + instance.getId());
        workers.add(instance);
    }

    @Override
    public synchronized void removeWorkerBalancer(WorkerInstance instance) {
        logger.info("Remove worker " + instance.getId());
        workers.remove(instance);
    }

}
