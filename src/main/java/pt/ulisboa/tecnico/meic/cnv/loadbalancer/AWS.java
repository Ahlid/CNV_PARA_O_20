package pt.ulisboa.tecnico.meic.cnv.loadbalancer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.LaunchTemplateSpecification;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.meic.cnv.loadbalancer.*;
import pt.ulisboa.tecnico.meic.cnv.storage.*;
import org.apache.log4j.Logger;

import java.util.*;

public class AWS {
    
    static AmazonEC2 ec2;
    static AmazonCloudWatch cloudWatch;
    
    final static Logger logger = Logger.getLogger(AWS.class);
    // AWS EC2 Endpoint (US - East North Virginia)
    private static final String REGION = "us-east-1";
    // Instance id to be used
    private static String AMI_ID = "ami-5ba10224";
    // Instance type to be used
    private static final String INST_TYPE = "t2.micro";
    // Key name
    private static final String KEY_NAME = "cnv1718";
    // Security Group that opens port 22 (ssh) and 8080 (http)
    private static final String SEC_GROUP = "CNV-worker-sg";
    
    // Worker launch template
    private static final String WORKER_TEMPLATE_NAME = "CNV-worker-template";
    private static String workerAmiId = null;
    
    private static RunInstancesRequest runInstanceReq = null;
    
    public static void init() throws Exception {
        
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
            "Cannot load the credentials from the credential profiles file. " +
            "Please make sure that your credentials file is at the correct " +
            "location (~/.aws/credentials), and is in valid format.",
            e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion(REGION).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        
        cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion(REGION).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public void setupInstances(){
        runInstanceReq = new RunInstancesRequest();
        runInstanceReq.withImageId(workerAmiId)
        .withInstanceType(INST_TYPE)
        .withMinCount(1)
        .withMaxCount(1)
        .withKeyName(KEY_NAME)
        .withSecurityGroups(SEC_GROUP)
        .withMonitoring(true);
    }
    
    public void setupInstanceRequest(int min, int max) {
        runInstanceReq = new RunInstancesRequest();
        runInstanceReq.withLaunchTemplate(new LaunchTemplateSpecification()
        .withLaunchTemplateName(WORKER_TEMPLATE_NAME))
        .withMinCount(min)
        .withMaxCount(max)
        ;
    }
    
    // Creates a new instance and returns the Instance ID
    public static WorkerInstance createInstance() {
        String result;
        RunInstancesResult instanceResult = ec2.runInstances(runInstanceReq);
        WorkerInstance worker = new WorkerInstance();
        
        worker.setId(instanceResult.getReservation().getInstances().get(0).getInstanceId());
        worker.setStatus(instanceResult.getReservation().getInstances().get(0).getState().getName());
        worker.setAddress(instanceResult.getReservation().getInstances().get(0).getPublicDnsName());
        return worker;
    }
    
    // Terminates the instance with the given ID
    public static void terminateInstance(String instanceId) {
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceId);
        ec2.terminateInstances(termInstanceReq);
    }
    
    // Get all instances and reservations
    public static ArrayList<WorkerInstance> getInstances() throws Exception{
        int runningInstances = 0;
        ArrayList<WorkerInstance> workers = new ArrayList<WorkerInstance>();
        
        // Get instances
        DescribeInstancesResult describeInstancesResult = AWS.ec2.describeInstances();
        List<Reservation> reservations = describeInstancesResult.getReservations();
        Set<Instance> instances = new HashSet<Instance>();
        
        //System.out.println("total reservations = " + reservations.size());
        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }
        
        /* TODO total observation time in milliseconds */
        long offsetInMilliseconds = 1000 * 60 * 24;
        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(instanceDimension);
        for (Instance instance : instances) {
            String instID = instance.getImageId();
            //System.out.println("image:" + instID);
            if (instID.equals(workerAmiId)) {
                String name = instance.getInstanceId();
                String state = instance.getState().getName();
                String address = instance.getPublicDnsName();
                if (state.equals("running") || state.equals("pending")) {
                    runningInstances++;
                    WorkerInstance worker = new WorkerInstance();
                    worker.setId(name);
                    worker.setStatus(state);
                    worker.setAddress(address);
                    
                    instanceDimension.setValue(name);
                    GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                    .withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
                    .withNamespace("AWS/EC2")
                    .withPeriod(60)
                    .withMetricName("CPUUtilization")
                    .withStatistics("Average", "Maximum")
                    .withDimensions(instanceDimension)
                    .withEndTime(new Date());
                    GetMetricStatisticsResult getMetricStatisticsResult =
                    cloudWatch.getMetricStatistics(request);
                    
                    List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
                    Collections.sort(datapoints, new Comparator<Datapoint>() {
                        @Override
                        public int compare(Datapoint o1, Datapoint o2) {
                            return o1.getTimestamp().compareTo(o2.getTimestamp());
                        }
                    });
                    logger.info("CPU USE for instance: " + name + " | Datapoints: " + datapoints.size());
                    for (Datapoint dp : datapoints) {
                        //logger.info("Average: " + dp.getAverage() + "| Max: " + dp.getMaximum() + " | Time: " + dp.getTimestamp());
                        worker.setCPU(dp.getAverage());
                    }
                    workers.add(worker);
                }
                
                else {
                }
            }
        }
        
        //logger.info("Running instances: " + runningInstances);
        
        return workers;
    }
    
    public void setWorkerAmiId(String amiId) {
        workerAmiId = amiId;
    }

    public String getWorkerAmiId() {
        return workerAmiId;
    }
    
    public static String getInstance(String instanceId) {
        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult instanceResult = ec2.describeInstances(describeInstanceRequest);
        //Instance instance = new Instance();
        //instance.setStatus(describeInstanceResult.getReservations().get(0).getInstances().get(0).getState().getCode());
        //instance.setId(describeInstanceResult.getReservations().get(0).getInstances().get(0).getInstanceId());
        //instance.setDns(describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicDnsName());
        return instanceResult.toString();
    }
    
    public static InstanceStatus getInstanceStatus(String instanceId) {
        DescribeInstanceStatusRequest disreq = new DescribeInstanceStatusRequest().withInstanceIds(instanceId);
        DescribeInstanceStatusResult disres = ec2.describeInstanceStatus(disreq);
        if (disres.getInstanceStatuses() != null && disres.getInstanceStatuses().size() > 0) {
            return disres.getInstanceStatuses().get(0);
        } else {
            return null;
        }
    }
    
    
}
