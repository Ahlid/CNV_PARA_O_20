package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

public class WorkerInstance {

    private String id = "unknown";
    private String status = "pending";
    private String address = "unknown";
    private Double cpu = 0.0;
    private Double progress = 0.0;
    private Integer jobs = 0;
    private Boolean working = false;


    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getAddress() {return address; }
    public Double getCPU() {return cpu; }
    public Double getProgress() {return progress;}
    public Integer getJobs() {return jobs;}
    public Boolean working() {return working;}


    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setAddress(String address) { this.address = address; }
    public void setCPU(Double cpu) { this.cpu = cpu; }
    public void setJobs(Integer jobs) {this.jobs = jobs;}
    public void setProgress(Double progress, Integer size) {
        this.progress = progress;
        this.jobs = jobs;
    }
    public void setWork(Boolean work){ this.working = work;}
}
