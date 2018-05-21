package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import java.util.ArrayList;
import java.util.List;

public class WorkerInstance {

    private String id = "unknown";
    private String status = "pending";
    private String address = "unknown";
    private Double cpu = 0.0;
    private Double progress = 0.0;
    private Integer jobs = 0;
    private Boolean working = false;
    private List<Job> myJobs = new ArrayList<>();


    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    public Double getCPU() {
        return cpu;
    }

    public Double getProgress() {
        return progress;
    }

    public Integer getJobs() {
        return jobs;
    }

    public Boolean working() {
        return working;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCPU(Double cpu) {
        this.cpu = cpu;
    }

    public void setJobs(Integer jobs) {
        this.jobs = jobs;
    }

    public void setProgress(Double progress, Integer size) {
        this.progress = progress;
        this.jobs = jobs;
    }

    public void setWork(Boolean work) {
        this.working = work;
    }

    public boolean addJob(Job b) {
        return this.myJobs.add(b);
    }

    public boolean removeJob(Job b) {
        return this.myJobs.remove(b);
    }

    public double getCurrentProgress() {

        synchronized (this.myJobs) {
            if (this.myJobs.size() == 0) {
                return 100;
            }

            double total = 0;

            for (Job j : myJobs) {
                if (j.getCurrentBBprocessed() > j.getExpecteCost()) {
                    total += 99;
                } else {
                    total += (j.getCurrentBBprocessed() / j.getExpecteCost()) * 100.0;
                }
            }

            return total / this.myJobs.size();
        }


    }

    public double getCurrentProgressInBB() {

        synchronized (this.myJobs) {
            if (this.myJobs.size() == 0) {
                return 0;
            }

            double total = 0;

            for (Job j : myJobs) {
                if (j.getCurrentBBprocessed() > j.getExpecteCost()) {
                    total += j.getExpecteCost();
                } else {
                    total += j.getCurrentBBprocessed();
                }
            }

            return total;
        }


    }
    public static class Job {
        private String id;
        private long expecteCost;
        private long currentBBprocessed;
        private WorkerInstance workerInstance;
        private boolean finished;
        private String requestType;

        public static final String RAPID_REQUEST = "RAPID_REQUEST";
        public static final String MEDIUM_REQUEST = "MEDIUM_REQUEST";
        public static final String LONG_REQUEST = "LONG_REQUEST";


        public Job(String id, long expecteCost) {
            this.id = id;
            this.expecteCost = expecteCost;
            this.currentBBprocessed = 0;
            this.requestType = getTypeOfRequest(expecteCost);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getExpecteCost() {
            return expecteCost;
        }

        public void setExpecteCost(long expecteCost) {
            this.expecteCost = expecteCost;
        }

        public long getCurrentBBprocessed() {
            return currentBBprocessed;
        }

        public void setCurrentBBprocessed(long currentBBprocessed) {
            this.currentBBprocessed = currentBBprocessed;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public WorkerInstance getWorkerInstance() {
            return workerInstance;
        }

        public void setWorkerInstance(WorkerInstance workerInstance) {
            this.workerInstance = workerInstance;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public void endJob() {
            if (this.workerInstance != null)
                this.workerInstance.removeJob(this);
        }

        public static String getTypeOfRequest(long basicBlocks) {
            if (basicBlocks < Scaler.rapidRequestLimit) {
                return RAPID_REQUEST;
            }

            if (basicBlocks > Scaler.longRequestLimit) {
                return LONG_REQUEST;
            }

            return MEDIUM_REQUEST;
        }

    }
}
