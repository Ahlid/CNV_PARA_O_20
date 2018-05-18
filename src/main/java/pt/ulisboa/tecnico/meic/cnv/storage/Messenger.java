package pt.ulisboa.tecnico.meic.cnv.storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.ulisboa.tecnico.meic.cnv.loadbalancer.*;
import org.apache.log4j.Logger;
//import pt.tecnico.ulisboa.meic.cnv.storage.AmazonDynamoDB;

import java.util.*;

public class Messenger {
    final static Logger logger = Logger.getLogger(Messenger.class);
    static AmazonDynamoDB db = null;

    private static final String AMI_TOPIC = "AMI Name";
    private static final String AMI_TOPIC_TABLE = "myConfig";

    private static final String METRICS_TOPIC = "Metrics";
    private static final String METRICS_TABLE = "Metrics";
    private static final String JOBS_TABLE = "Jobs";

    public Messenger() throws Exception {
        db = new AmazonDynamoDB();
        db.init();
    }

    public void setup() throws Exception{

        // Create table to keep settings
        String tableNameConfig = AMI_TOPIC_TABLE;

        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableNameConfig)
                .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(db.dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(db.dynamoDB, tableNameConfig);

        // Describe our new table
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableNameConfig);
        TableDescription tableDescription = db.dynamoDB.describeTable(describeTableRequest).getTable();
        //System.out.println("Config Table Description: " + tableDescription + "\n");

        // setup table for metrics
        String tableName = METRICS_TABLE;

        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest1 = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(db.dynamoDB, createTableRequest1);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(db.dynamoDB, tableName);

        // Describe our new table
        DescribeTableRequest describeTableRequest1 = new DescribeTableRequest().withTableName(tableName);
        TableDescription tableDescription1 = db.dynamoDB.describeTable(describeTableRequest1).getTable();
        //System.out.println("Metrics Table Description: " + tableDescription1 + "\n");

        // setup table for metrics
        String tableName2 = JOBS_TABLE;

        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest2 = new CreateTableRequest().withTableName(tableName2)
                .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(db.dynamoDB, createTableRequest2);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(db.dynamoDB, tableName2);

        // Describe our new table
        DescribeTableRequest describeTableRequest2 = new DescribeTableRequest().withTableName(tableName2);
        TableDescription tableDescription2 = db.dynamoDB.describeTable(describeTableRequest2).getTable();
        //System.out.println("Metrics Table Description: " + tableDescription1 + "\n");
    }

    // change name of AMI image for workers instances
    public int changeAmiName(String payload){
        // puts messages in Configuration table
        Map<String, AttributeValue> item = newItem(AMI_TOPIC, payload);
        PutItemRequest putItemRequest = new PutItemRequest(AMI_TOPIC_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // get AMI image name for workers
    public String getAMIName() {
        // get messages from Configuration table
        String name = null;
        try {
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(AMI_TOPIC));
            scanFilter.put("name", condition);
            ScanRequest scanRequest = new ScanRequest(AMI_TOPIC_TABLE).withScanFilter(scanFilter);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                name = i.get("value").getS();
            }
            return name;
        } catch (Exception e) {
            logger.error("null" + e.getMessage());
            return "null";
        }
    }

    // put messages in Metrics table
    public int newWorker(String id, String ip){
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newMetricsItem(id, "pending",0.0, ip);
        PutItemRequest putItemRequest = new PutItemRequest(METRICS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // put messages in Metrics table
    public int workerUpdate(String id, String progress,int size ,Boolean finish){
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newJobItem(id, progress,size,finish );
        PutItemRequest putItemRequest = new PutItemRequest(JOBS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // put messages in Metrics table
    public int putMessage(WorkerInstance instance){
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newMetricsItem(instance.getId(), instance.getStatus(), instance.getCPU(), instance.getAddress());
        PutItemRequest putItemRequest = new PutItemRequest(METRICS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    public List<String> getProgress(String id){
        // get messages from Metrics table
        List<String> result = null;
        try {
            result = new ArrayList<>();
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(id));
            scanFilter.put("id", condition);
            ScanRequest scanRequest = new ScanRequest(JOBS_TABLE).withScanFilter(scanFilter);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()){
                result.add(i.get("progress").getS());
                result.add(i.get("size").getS());
                result.add(String.valueOf(i.get("finish").getBOOL()));
            }
            return result;
        }
        catch (Exception e){
            logger.error( "exception: " + e.getMessage());
        }
        return result;
    }


    // retrieve metrics for given worker from Metrics table
    public List<String> getMessage(WorkerInstance worker){
        // get messages from Metrics table
            List<String> result = new ArrayList<>();
            System.out.println(worker.getId());
            try {
                HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                Condition condition = new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ.toString())
                        .withAttributeValueList(new AttributeValue(worker.getId()));
                scanFilter.put("id", condition);
                ScanRequest scanRequest = new ScanRequest(METRICS_TABLE).withScanFilter(scanFilter);
                ScanResult scanResult = db.dynamoDB.scan(scanRequest);
                for (Map<String, AttributeValue> i : scanResult.getItems()){
                    result.add(i.get("id").getS());
                    result.add(i.get("state").getS());
                    result.add(i.get("cpu").getS());
                }
                return result;
            }
            catch (Exception e){
                logger.error( "exception: " + e.getMessage());
            }
        return result;
    }

    private static Map<String, AttributeValue> newItem(String name, String value) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("name", new AttributeValue(name));
        item.put("value", new AttributeValue(value));

        return item;
    }

    private static Map<String, AttributeValue> newMetricsItem(String id, String state, Double cpu, String address) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue(id));
        item.put("state", new AttributeValue(state));
        item.put("cpu", new AttributeValue().withS(cpu.toString()));
        item.put("address", new AttributeValue(address));

        return item;
    }

    private static Map<String, AttributeValue> newJobItem(String id, String progress,int size, Boolean finish) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue(id));
        item.put("progress", new AttributeValue(progress));
        item.put("size", new AttributeValue(String.valueOf(size)));
        item.put("finish", new AttributeValue().withBOOL(finish));

        return item;
    }

    public static void listMyTables() {

        ListTablesResult tables = db.dynamoDB.listTables();
        List<String> tableNames = tables.getTableNames();

        logger.info("Listing table names");

        for(String n : tableNames){
            System.out.println(n);
        }
    }

    public static void deleteTable(String tableName) {

        //Table table = db.dynamoDB.getTable(tableName);
        try {
            logger.info("Issuing DeleteTable request for " + tableName);
            db.dynamoDB.deleteTable(tableName);

            logger.info("Waiting for " + tableName + " to be deleted...this may take a while...");

            //table.waitForDelete();
        }
        catch (Exception e) {
            logger.error("DeleteTable request failed for " + tableName);
            logger.error(e.getMessage());
        }
    }
}
