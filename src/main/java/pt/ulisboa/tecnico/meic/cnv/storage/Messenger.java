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
    
    private static final String CONFIG_TABLE = "myConfig";
    
    private static final String WORKERS_TABLE = "WORKERS";
    
    private static final String METRICS_TOPIC = "Metrics";
    private static final String METRICS_TABLE = "Metrics";
    private static final String JOBS_TABLE = "Jobs";
    
    public Messenger() throws Exception {
        db = new AmazonDynamoDB();
        db.init();
    }
    
    public void setup() throws Exception{
        logger.info("Initializing DynamoDB...");
        createConfigTable();
        createIDTable(WORKERS_TABLE);
        createIDTable(METRICS_TABLE);
        createIDTable(JOBS_TABLE);
    }
    
    /*
    * 
    * Workers Functions
    *
    */
    
    // put messages in Metrics table
    public int newWorker(String id, String ip){
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newWorkerItem(id, "pending", 0.0, ip, false, 0.0);
        PutItemRequest putItemRequest = new PutItemRequest(WORKERS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }
    
    // put messages in Metrics table
    public int putMessage(WorkerInstance instance){
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newWorkerItem(instance.getId(), instance.getStatus(), 
                                                        instance.getCPU(), instance.getAddress(), 
                                                        instance.working(), instance.getProgress());
        PutItemRequest putItemRequest = new PutItemRequest(WORKERS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }
    
    
    private static Map<String, AttributeValue> newWorkerItem(String id, String status, Double cpu, String address, Boolean working, Double progress) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue(id));
        item.put("status", new AttributeValue(status));
        item.put("cpu", new AttributeValue().withS(cpu.toString()));
        item.put("address", new AttributeValue(address));
        item.put("working", new AttributeValue().withBOOL(working));
        item.put("progress", new AttributeValue().withS(progress.toString()));
        
        return item;
    }

    /*
    * 
    * Metrics Functions
    *
    */

    // put messages in Jobs table
    public int workerUpdate(String id, String progress,int size ,Boolean finish){
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newJobItem(id, progress,size,finish );
        PutItemRequest putItemRequest = new PutItemRequest(JOBS_TABLE, item);
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
    
    /*
    *
    * Configuration table
    * 
    */
    private void createConfigTable() throws Exception{
        // Create table to keep settings
        String tableNameConfig = CONFIG_TABLE;
        
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
    }
    
    // change name of AMI image for workers instances
    public int changeAmiName(String payload){
        // puts messages in Configuration table
        Map<String, AttributeValue> item = newItem("AMI_Name", payload);
        PutItemRequest putItemRequest = new PutItemRequest(CONFIG_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }
    
    // change name of AMI image for workers instances
    public int changeConfig(String name, String value){
        // puts messages in Configuration table
        Map<String, AttributeValue> item = newItem(name, value);
        PutItemRequest putItemRequest = new PutItemRequest(CONFIG_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }
    
    public static LinkedHashMap<String, String> fetchConfig() {
        // get messages from Metrics table
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try {
            
            ScanRequest scanRequest = new ScanRequest(CONFIG_TABLE);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()){
                result.put((String)i.get("name").getS(),(String)i.get("value").getS());
            }
            return result;
        }
        catch (Exception e){
            logger.error( "exception: " + e.getMessage());
        }
        return result;
    }
    
    // get AMI image name for workers
    public String getAMIName() {
        // get messages from Configuration table
        String name = null;
        try {
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
            .withComparisonOperator(ComparisonOperator.EQ.toString())
            .withAttributeValueList(new AttributeValue("AMI_Name"));
            scanFilter.put("name", condition);
            ScanRequest scanRequest = new ScanRequest(CONFIG_TABLE).withScanFilter(scanFilter);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                name = i.get("value").getS();
            }
            return name;
        } catch (Exception e) {
            logger.error("null" + e.getMessage());
            return "empty";
        }
    }
    
    
    
    /*
    *
    * Generic Tables
    * 
    */
    private void createIDTable(String name) throws Exception{
        // Create table to keep settings
        String tableName = name;
        
        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
        .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
        .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
        .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
        
        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(db.dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(db.dynamoDB, tableName);
        
        // Describe our new table
        DescribeTableRequest describeTableRequest1 = new DescribeTableRequest().withTableName(tableName);
        TableDescription tableDescription1 = db.dynamoDB.describeTable(describeTableRequest1).getTable();
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
