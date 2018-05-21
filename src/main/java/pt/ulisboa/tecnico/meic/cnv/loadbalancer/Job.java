package pt.ulisboa.tecnico.meic.cnv.loadbalancer;


public class Job {
    private String id;
    private int expecteCost;
    private int currentBBprocessed;
    private WorkerInstance workerInstance;
    private boolean finished;


    public Job(String id, int expecteCost) {
        this.id = id;
        this.expecteCost = expecteCost;
        this.currentBBprocessed = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getExpecteCost() {
        return expecteCost;
    }

    public void setExpecteCost(int expecteCost) {
        this.expecteCost = expecteCost;
    }

    public int getCurrentBBprocessed() {
        return currentBBprocessed;
    }

    public void setCurrentBBprocessed(int currentBBprocessed) {
        this.currentBBprocessed = currentBBprocessed;
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
}
