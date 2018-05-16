package pt.ulisboa.tecnico.meic.cnv.loadbalancer;


public interface Balancer {
    public void addWorkerBalancer(WorkerInstance instance);
    public void removeWorkerBalancer(WorkerInstance instance);
    public WorkerInstance getInstance();
    public void setScaler(Scaler scaler);
}
