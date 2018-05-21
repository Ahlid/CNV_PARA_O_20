package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import java.util.ArrayList;
import java.util.List;

public class JobsPool {

    private static JobsPool instance;
    private List<WorkerInstance.Job> jobs;

    private JobsPool() {
        this.jobs = new ArrayList<>();
    }

    public static JobsPool getInstance() {
        if (instance == null) {
            instance = new JobsPool();
        }

        return instance;
    }

    public boolean addJob(WorkerInstance.Job b) {
        return this.jobs.add(b);
    }

    public boolean endJob(WorkerInstance.Job b) {
        synchronized (jobs) {
            b.setFinished(true);
            b.endJob();
            return this.jobs.remove(b);
        }
    }

    public void serJobBBWork(String jobId, long bb) {
        synchronized (this.jobs) {
            for (WorkerInstance.Job job : this.jobs) {
                if (job.getId().equals(jobId)) {
                    job.setCurrentBBprocessed(bb);
                }
            }
        }
    }

    public List<WorkerInstance.Job> getJobs() {
        return jobs;
    }
}
