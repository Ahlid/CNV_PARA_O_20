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
    private static final String CACHE_TABLE = "CACHE";
    private static final String REQUEST_COST_TABLE = "REQUEST_COST_TABLE";

    private static final String METRICS_TABLE = "Metrics";

    private static Messenger instance;

    private Messenger() throws Exception {
        db = new AmazonDynamoDB();
        db.init();
    }

    public void setup() throws Exception {
        logger.info("Initializing DynamoDB...");
        createTable(CONFIG_TABLE, "name");
        createTable(WORKERS_TABLE, "id");
        createPartitionKeyTable(CACHE_TABLE, "id", "strategy");
        createPartitionKeyTable(METRICS_TABLE, "id", "requestId");
        //System.out.println(getWorkersIds());
        //System.out.println(getCache("{m=Maze50.maze, x0=1, y0=1, x1=6, y1=6, v=75, s=bfs}"));
        createRequestCostTable();
    }

    public static Messenger getInstance() {
        if (instance == null) {
            try {
                instance = new Messenger();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    /*
     *
     * Workers Functions
     *
     */

    // Adds a new worker in Workers table
    public int newWorker(String id, String hostname) {
        // instanceid, status, cpu, endpoint, working, jobs
        Map<String, AttributeValue> item = newWorkerItem(id, "pending", 0.0, hostname, false, 0);
        PutItemRequest putItemRequest = new PutItemRequest(WORKERS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // Set end state for worker in Workers table
    public int endWorker(String id) {
        Map<String, AttributeValue> item = newDeadWorkerItem(id, "dead");
        PutItemRequest putItemRequest = new PutItemRequest(WORKERS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // put messages in Workers table
    public int workerUpdate(String id, String status, Double cpu, String hostname, Boolean working, Integer jobs) {
        Map<String, AttributeValue> item = newWorkerItem(id, status, cpu, hostname, working, jobs);
        PutItemRequest putItemRequest = new PutItemRequest(WORKERS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // put messages in Workers table
    public int putMessage(WorkerInstance instance) {
        Map<String, AttributeValue> item = newWorkerItem(instance.getId(), instance.getStatus(),
                instance.getCPU(), instance.getAddress(),
                instance.working(), instance.getJobs());
        PutItemRequest putItemRequest = new PutItemRequest(WORKERS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }


    private static Map<String, AttributeValue> newWorkerItem(String id, String status, Double cpu, String address, Boolean working, Integer jobs) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        Map<String, AttributeValue> metric = newWorkerStatsItem(cpu, address, working, jobs);
        item.put("id", new AttributeValue(id));
        item.put("status", new AttributeValue(status));
        item.put("stats", new AttributeValue().withM(metric));

        return item;
    }

    private static Map<String, AttributeValue> newWorkerStatsItem(Double cpu, String address, Boolean working, Integer jobs) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("cpu", new AttributeValue().withS(cpu.toString()));
        item.put("address", new AttributeValue(address));
        item.put("working", new AttributeValue().withBOOL(working));
        item.put("jobs", new AttributeValue().withS(jobs.toString()));

        return item;
    }

    private static Map<String, AttributeValue> newDeadWorkerItem(String id, String status) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue(id));
        item.put("status", new AttributeValue(status));

        return item;
    }

    public static List<String> getWorkersIds() {
        // get messages from workers table
        List<String> result = null;
        try {

            ScanRequest scanRequest = new ScanRequest(WORKERS_TABLE);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                logger.info(scanResult.getItems());
                result.add((String) i.get("id").getS());
            }
            return result;
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
        }
        return result;
    }

    public static LinkedHashMap<String, Map<String, String>> getWorkersTable() {
        // get messages from workers table
        LinkedHashMap<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> stats = new LinkedHashMap<>();
        try {

            ScanRequest scanRequest = new ScanRequest(WORKERS_TABLE);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                stats.put("cpu", i.get("stats").getM().get("address").getS());
                stats.put("address", i.get("stats").getM().get("address").getS());
                stats.put("working", String.valueOf(i.get("stats").getM().get("working").getBOOL()));
                stats.put("progress", i.get("stats").getM().get("progress").getS());
                stats.put("status", i.get("status").getS());
                result.put((String) i.get("id").getS(), stats);
            }
            return result;
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
        }
        return result;
    }

    public static LinkedHashMap<String, Map<String, String>> getCacheTable() {
        // get messages from workers table
        LinkedHashMap<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> stats = new LinkedHashMap<>();
        try {

            ScanRequest scanRequest = new ScanRequest(CACHE_TABLE);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                stats.put("request", i.get("request").getS());
                stats.put("bb", i.get("bb").getS());
                stats.put("strategy", i.get("strategy").getS());
                stats.put("maze", i.get("maze").getS());
                result.put((String) i.get("request").getS(), stats);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("exception: " + e.getMessage());
        }
        return result;
    }

    /*
     *
     * Request Cost Functions
     *
     */

    public void newRequestCost(String instanceId, String aLong, String params) {
        // puts messages in cache table
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("id", new AttributeValue(instanceId));
        item.put("requestId", new AttributeValue(aLong));
        item.put("params", new AttributeValue(params));
        item.put("cost", new AttributeValue("0"));
        item.put("terminated", new AttributeValue().withBOOL(false));

        PutItemRequest putItemRequest = new PutItemRequest(REQUEST_COST_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
    }

    public static LinkedHashMap<String, Map<String, String>> getRequestCostTable() {
        // get messages from workers table
        LinkedHashMap<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> stats = new LinkedHashMap<>();
        try {

            ScanRequest scanRequest = new ScanRequest(REQUEST_COST_TABLE);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                stats.put("id", i.get("id").getS());
                stats.put("requestId", i.get("requestId").getS());
                stats.put("expectedCost", i.get("expectedCost").getS());
                result.put((String) i.get("id").getS(), stats);
            }
            return result;
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
        }
        return result;
    }

    private void createRequestCostTable() throws Exception {
        // Create table to keep settings
        String tableName = REQUEST_COST_TABLE;

        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName);
        createTableRequest.withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("requestId").withKeyType(KeyType.RANGE));
        //createTableRequest.withLocalSecondaryIndexes(makeLocalSecondaryIndexes());
        createTableRequest.withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S),
                new AttributeDefinition().withAttributeName("requestId").withAttributeType(ScalarAttributeType.S));
        createTableRequest.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(10L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(db.dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(db.dynamoDB, tableName);

        // Describe our new table
        DescribeTableRequest describeTableRequest1 = new DescribeTableRequest().withTableName(tableName);
        TableDescription tableDescription1 = db.dynamoDB.describeTable(describeTableRequest1).getTable();
    }

    /*
     *
     * Metrics Functions
     *
     */
    private static Map<String, AttributeValue> newMetricsItem(String instanceId, String requestId, String inst, String bb, Boolean finished, String params) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        Map<String, AttributeValue> metric = newMItem(inst, bb);
        //Map<String, AttributeValue> metric = newMItem( inst,  bb);
        item.put("id", new AttributeValue(instanceId));
        item.put("requestId", new AttributeValue(requestId));
        item.put("finished", new AttributeValue().withBOOL(finished));
        item.put("metrics", new AttributeValue().withM(metric));
        item.put("params", new AttributeValue(params));

        return item;
    }

    private static Map<String, AttributeValue> newMItem(String inst, String bb) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("inst", new AttributeValue(inst));
        item.put("bb", new AttributeValue(bb));
        return item;
    }


    // put messages in Metrics table
    public int newMetrics(String instanceId, String requestId, String inst, String bb, Boolean finished, String params) {
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newMetricsItem(instanceId, requestId, inst, bb, finished, params);

        PutItemRequest putItemRequest = new PutItemRequest(METRICS_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    public List<String> getProgress(String id) {
        // get messages from Metrics table
        List<String> result = null;
        try {
            result = new ArrayList<>();
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(id));
            scanFilter.put("id", condition);
            ScanRequest scanRequest = new ScanRequest(METRICS_TABLE).withScanFilter(scanFilter);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                result.add(i.get("request").getS());
                result.add(i.get("bb").getS());
                result.add(String.valueOf(i.get("finished").getBOOL()));
            }
            return result;
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
        }
        return result;
    }

    /*
     *
     * Cache Functions
     *
     */
    private static Map<String, AttributeValue> newCacheItem(String request, String bb, String inst) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue(request));
        item.put("bb", new AttributeValue(bb));
        item.put("inst", new AttributeValue(inst));
        item.put("strategy", new AttributeValue(request.split("s=")[1].split("}")[0]));


        item.put("maze", new AttributeValue(request.split("m=")[1].split(",")[0].substring(0, request.split("m=")[1].split(",")[0].length() - 5)));

        return item;
    }

    // put messages in Metrics table
    public int newCacheMetrics(String request, String bb, String inst) {
        // puts messages in Metrics table
        Map<String, AttributeValue> item = newCacheItem(request, bb, inst);
        PutItemRequest putItemRequest = new PutItemRequest(CACHE_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    public List<String> getCache(String request) {
        // get bb from Cache table
        List<String> result = null;
        try {
            result = new ArrayList<>();
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(request));
            scanFilter.put("id", condition);
            ScanRequest scanRequest = new ScanRequest(CACHE_TABLE).withScanFilter(scanFilter);
            ScanResult scanResult = db.dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                result.add(i.get("bb").getS());
            }
            return result;
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
        }
        return result;
    }

    /*
     *
     * Configuration functions
     *
     */

    // Config item 
    private static Map<String, AttributeValue> newConfigItem(String name, String value) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("name", new AttributeValue(name));
        item.put("value", new AttributeValue(value));

        return item;
    }

    // change name of AMI image for workers instances
    public int changeAmiName(String payload) {
        // puts messages in Configuration table
        Map<String, AttributeValue> item = newConfigItem("AMI_Name", payload);
        PutItemRequest putItemRequest = new PutItemRequest(CONFIG_TABLE, item);
        PutItemResult putItemResult = db.dynamoDB.putItem(putItemRequest);
        return 1;
    }

    // change name of AMI image for workers instances
    public int changeConfig(String name, String value) {
        // puts messages in Configuration table
        Map<String, AttributeValue> item = newConfigItem(name, value);
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
            for (Map<String, AttributeValue> i : scanResult.getItems()) {
                result.put((String) i.get("name").getS(), (String) i.get("value").getS());
            }
            return result;
        } catch (Exception e) {
            logger.error("exception: " + e.getMessage());
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
    private void createTable(String name, String key) throws Exception {
        // Create table to keep settings
        String tableName = name;

        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName);
        createTableRequest.withKeySchema(new KeySchemaElement().withAttributeName(key).withKeyType(KeyType.HASH));
        createTableRequest.withAttributeDefinitions(new AttributeDefinition().withAttributeName(key).withAttributeType(ScalarAttributeType.S));
        createTableRequest.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(10L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(db.dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(db.dynamoDB, tableName);

        // Describe our new table
        DescribeTableRequest describeTableRequest1 = new DescribeTableRequest().withTableName(tableName);
        TableDescription tableDescription1 = db.dynamoDB.describeTable(describeTableRequest1).getTable();
    }

    // Create table with partition key
    private void createPartitionKeyTable(String name, String key, String sort) throws Exception {
        // Create table to keep settings
        String tableName = name;

        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName);
        createTableRequest.withKeySchema(new KeySchemaElement().withAttributeName(key).withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName(sort).withKeyType(KeyType.RANGE));
        createTableRequest.withAttributeDefinitions(new AttributeDefinition().withAttributeName(key).withAttributeType(ScalarAttributeType.S),
                new AttributeDefinition().withAttributeName(sort).withAttributeType(ScalarAttributeType.S));
        createTableRequest.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(10L));

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

        for (String n : tableNames) {
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
        } catch (Exception e) {
            logger.error("DeleteTable request failed for " + tableName);
            logger.error(e.getMessage());
        }
    }
}
