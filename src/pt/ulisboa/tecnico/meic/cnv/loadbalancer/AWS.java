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
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.meic.cnv.loadbalancer.*;
import pt.ulisboa.tecnico.meic.cnv.storage.*;
import org.apache.log4j.Logger;

import java.util.*;

public class AWS {
    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    static AmazonEC2 ec2;
    static AmazonCloudWatch cloudWatch;

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */

    final static Logger logger = Logger.getLogger(AWS.class);
    // AWS EC2 Endpoint (Ireland)
    private static final String REGION = "us-east-1";
    // Instance id to be used
    private static String AMI_ID = "ami-5ba10224";
    // Instance type to be used
    private static final String INST_TYPE = "t2.micro";
    // Key name
    private static final String KEY_NAME = "cnv1718";
    // Security Group that opens port 22 (ssh) and 8080 (http)
    private static final String SEC_GROUP = "CNV-ssh+http";

    private static RunInstancesRequest runInstanceReq = null;

    public static void init() throws Exception {


        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
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

        Messenger m = new Messenger();

        AMI_ID = m.getAMIName();

        runInstanceReq = new RunInstancesRequest();
        runInstanceReq.withImageId(AMI_ID)
                .withInstanceType(INST_TYPE)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SEC_GROUP)
                .withMonitoring(true);
    }


    // Creates a new instance and returns the Instance ID
    public static WorkerInstance createInstance() {
        String result;
        RunInstancesResult instanceResult = ec2.runInstances(runInstanceReq);
        WorkerInstance worker = new WorkerInstance();
        worker.setId(instanceResult.getReservation().getInstances().get(0).getInstanceId());
        worker.setStatus(instanceResult.getReservation().getInstances().get(0).getState().toString());
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
        //System.out.println("total instances = " + instances.size());

            /* TODO total observation time in milliseconds */
        long offsetInMilliseconds = 1000 * 60 * 24;
        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(instanceDimension);
        for (Instance instance : instances) {
            String instID = instance.getImageId();
            //System.out.println("image:" + instID);
            if (instID.equals(AMI_ID)) {
                String name = instance.getInstanceId();
                String state = instance.getState().getName();
                String address = instance.getPublicDnsName();
                if (state.equals("running") || state.equals("pending")) {
                    runningInstances++;
                    WorkerInstance worker = new WorkerInstance();
                    worker.setId(name);
                    worker.setStatus(state);
                    worker.setAddress(address);

                    //System.out.println("running instance id = " + name);
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
                    //List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
                    // sort result set
                     List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
                    Collections.sort(datapoints, new Comparator<Datapoint>() {
                        @Override
                        public int compare(Datapoint o1, Datapoint o2) {
                            return o1.getTimestamp().compareTo(o2.getTimestamp());
                        }
                    });
                    System.out.println("printing dp - size: " + datapoints.size());
                    for (Datapoint dp : datapoints) {
                        System.out.println(" CPU utilization for instance " + name +
                                " = " + dp.getAverage() + " max:" + dp.getMaximum() + " @ " + dp.getTimestamp());
                        worker.setCPU(dp.getAverage());
                        }
                    workers.add(worker);
                    }

            else {
            //    System.out.println("instance id = " + name);
            }
            //System.out.println("Instance State : " + state +".");
        }
        }

        System.out.println("running instances: " + runningInstances);

        return workers;
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
