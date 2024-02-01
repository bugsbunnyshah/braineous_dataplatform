package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.CatalogDatabaseImpl;
import org.apache.flink.table.catalog.hive.HiveCatalog;
import org.apache.flink.types.Row;

import org.bson.Document;
import org.ehcache.sizeof.SizeOf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class JobManager {
    private static Logger logger = LoggerFactory.getLogger(JobManager.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SchemalessMapper schemalessMapper;

    private Map<String,Long> pipeToOffset = new HashMap<>();

    private ExecutorService submitJobPool = Executors.newFixedThreadPool(25);
    private ExecutorService retryJobPool = Executors.newFixedThreadPool(25);

    private String table;

    @PostConstruct
    public void start(){

    }

    public synchronized void submit(StreamExecutionEnvironment env, SecurityToken securityToken,
                       String driverConfiguration, String entity,
                       String pipeId, long offset, String jsonString){
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonString);

            JsonArray ingestion = new JsonArray();
            if (jsonElement.isJsonArray()) {
                ingestion = jsonElement.getAsJsonArray();
            } else if (jsonElement.isJsonObject()) {
                ingestion.add(jsonElement);
            }

            //ingestion array
            List<Map<String, Object>> flatArray = new ArrayList();
            for (int i = 0; i < ingestion.size(); i++) {
                JsonObject jsonObject = ingestion.get(i).getAsJsonObject();
                Map<String, Object> flatJson = this.schemalessMapper.mapAll(jsonObject.toString());
                flatArray.add(flatJson);
            }

            if (this.table == null) {
                this.table = this.createTable(env);
            }


            submitJob(env,
                    flatArray,
                    securityToken,
                    table,
                    driverConfiguration,
                    pipeId,
                    entity);
        }catch(Exception e){
            logger.error(e.getMessage(), e);

            //TODO: handle system level errors
        }
    }

    private synchronized void submitJob(StreamExecutionEnvironment env, List<Map<String, Object>> flatArray,
                                        SecurityToken securityToken,
                                        String table,
                                        String driverConfiguration,
                                        String pipeId,
                                        String entity){
        submitJobPool.execute(() -> {
            try {
                this.addData(env, table, flatArray);
                logger.info("****JOB_SUCCESS*****");
            }catch(Exception e){
                this.retryJob(env, table, flatArray);
            }
        });
    }

    private void retryJob(StreamExecutionEnvironment env,
                          String table,
                          List<Map<String, Object>> flatArray){
        retryJobPool.execute(() -> {
            while(true){
                try {
                    this.addData(env, table, flatArray);
                    logger.info("****JOB_RETRY_SUCCESS*****");
                    break;
                }catch(Exception e){

                }
            }
        });
    }

    private String createTable(StreamExecutionEnvironment env) throws Exception{
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create a HiveCatalog
        String name            = "myhive";
        String database = "mydatabase";
        String hiveConfDir     = "/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf";
        String tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = name + "." + database + "." + tableName;

        HiveCatalog hive = new HiveCatalog(name, null, hiveConfDir);
        tableEnv.registerCatalog(name, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(name);

        // Create a catalog database
        hive.createDatabase(database,
                new CatalogDatabaseImpl(new HashMap<>(), "db_metadata"), true);

        // Create a catalog table
        final Schema schema = Schema.newBuilder()
                .column("name", DataTypes.STRING())
                .column("age", DataTypes.INT())
                .build();

        TableDescriptor tableDescriptor = TableDescriptor.forConnector("filesystem")
                .option("path", "file:///Users/babyboy/datalake/"+tableName)
                .option("format", "csv")
                .schema(schema)
                .build();

        tableEnv.createTable(table, tableDescriptor);

        return table;
    }

    private void addData(StreamExecutionEnvironment env, String table, List<Map<String, Object>> flatArray) throws Exception
    {
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create a HiveCatalog
        String name            = "myhive";
        String hiveConfDir     = "/Users/babyboy/mumma/braineous/infrastructure/apache-hive-3.1.3-bin/conf";

        HiveCatalog hive = new HiveCatalog(name, null, hiveConfDir);
        tableEnv.registerCatalog(name, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(name);


        String sql = "INSERT INTO " + table + " VALUES"
                + "  ('shah', 46), "
                + "  ('blah', 55)";

        final TableResult insertionResult =
                tableEnv.executeSql(sql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();
    }

    private boolean submitJob(StreamExecutionEnvironment env, List<Map<String, Object>> flatArray)
    {
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // create a table with example data without a connector required
        List<Row> rows = this.rows(flatArray);
        final Table ingestionTable = tableEnv.fromValues(rows);

        String sql = "SELECT * FROM "
                + ingestionTable;

        final Table result =
                tableEnv.sqlQuery(sql);

        tableEnv.toDataStream(result, Row.class).print();

        // after the table program is converted to a DataStream program,
        // we must use `env.execute()` to submit the job
        boolean success = true;
        try {
            env.execute();
        }catch (Exception e){
            success = false;
        }
        return success;
    }

    private List<Row> rows(List<Map<String, Object>> flatArray){
        List<Row> rows = new ArrayList<>();

        for(Map<String,Object> flatJson:flatArray) {
            Row row = this.row(flatJson);
            rows.add(row);
        }

        return rows;
    }

    private Row row(Map<String,Object> flatJson){
        Collection<Object> values = flatJson.values();

        Row row = new Row(values.size());

        int i =0;
        Set<Map.Entry<String,Object>> entrySet = flatJson.entrySet();
        for(Map.Entry<String,Object> entry: entrySet){
            String name = entry.getKey();
            Object value = entry.getValue();
            row.setField(i, value);
            i++;
        }

        return row;
    }
    //-----------------------------------------------------------------------------------------------------------
    private void preProcess(String value,
                            SecurityToken securityToken,
                            String pipeId){
        String principal = securityToken.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        //setup driver components
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = db.getCollection("pipeline_monitoring");

        Queue<String> queue = new LinkedList<>();
        queue.add(value);
        SizeOf sizeOf = SizeOf.newInstance();
        long dataStreamSize = sizeOf.deepSizeOf(queue);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pipeId", pipeId);
        jsonObject.addProperty("message", value);
        jsonObject.addProperty("sizeInBytes", dataStreamSize);
        jsonObject.addProperty("incoming", true);

        collection.insertOne(Document.parse(jsonObject.toString()));
    }

}
