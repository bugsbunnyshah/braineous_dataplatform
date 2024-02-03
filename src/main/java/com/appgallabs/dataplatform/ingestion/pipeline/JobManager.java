package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

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
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.ObjectPath;
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

    @Inject
    private DataLakeTableGenerator dataLakeTableGenerator;

    @Inject
    private DataLakeSessionManager dataLakeSessionManager;

    @Inject
    private DataLakeSqlGenerator dataLakeSqlGenerator;

    private Map<String,Long> pipeToOffset = new HashMap<>();

    private ExecutorService submitJobPool = Executors.newFixedThreadPool(25);
    private ExecutorService retryJobPool = Executors.newFixedThreadPool(25);


    @PostConstruct
    public void start(){

    }

    public synchronized void submit(StreamExecutionEnvironment env, SecurityToken securityToken,
                       String driverConfiguration, String entity,
                       String pipeId, long offset, String jsonString){
        try {
            //tenant
            Tenant tenant = new Tenant(securityToken.getPrincipal());

            String catalog = tenant.getPrincipal().replaceAll("-","").toLowerCase();
            String database = pipeId.replaceAll("-", "").toLowerCase();
            String tableName = entity.replaceAll("-", "").toLowerCase();


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

            Map<String, Object> row = flatArray.get(0);
            String table = this.createTable(env, catalog, database, tableName, row);

            //asynchronous
            this.submitJob(env, catalog, table, flatArray);
        }catch(Exception e){
            logger.error(e.getMessage(), e);

            //TODO: handle system level errors
        }
    }

    private String createTable(StreamExecutionEnvironment env, String catalogName,
            String database, String tableName, Map<String,Object> row) throws Exception{
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeSessionWithNewDatabase(
                env,
                catalogName,
                database
        );

        //String tableName = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        String table = catalogName + "." + database + "." + tableName;
        String objectPath = database + "." + tableName;
        String filePath = "file:///Users/babyboy/datalake/"+tableName;
        String format = "csv";

        String currentCatalog = tableEnv.getCurrentCatalog();
        Optional<Catalog> catalog = tableEnv.getCatalog(currentCatalog);
        boolean tableExists = catalog.get().tableExists(ObjectPath.fromString(objectPath));

        if(!tableExists) {
            // Create a catalog table
            TableDescriptor tableDescriptor = this.dataLakeTableGenerator.createFileSystemTable(row,
                    filePath,
                    format);

            tableEnv.createTable(table, tableDescriptor);
        }

        return table;
    }

    private synchronized void submitJob(StreamExecutionEnvironment env,
                                        String catalogName,
                                        String table,
                                        List<Map<String, Object>> flatArray){
        submitJobPool.execute(() -> {
            try {
                this.addData(env,catalogName, table, flatArray);
                logger.info("****JOB_SUCCESS*****");
            }catch(Exception e){
                this.retryJob(env, catalogName, table, flatArray);
            }
        });
    }

    private void retryJob(StreamExecutionEnvironment env,
                          String catalogName,
                          String table,
                          List<Map<String, Object>> flatArray){
        retryJobPool.execute(() -> {
            while(true){
                try {
                    this.addData(env, catalogName, table, flatArray);
                    logger.info("****JOB_RETRY_SUCCESS*****");
                    break;
                }catch(Exception e){

                }
            }
        });
    }

    private void addData(StreamExecutionEnvironment env,
                         String catalogName,
                         String table,
                         List<Map<String, Object>> rows) throws Exception
    {
        final StreamTableEnvironment tableEnv = this.dataLakeSessionManager.newDataLakeCatalogSession(
                env,
                catalogName
        );


        String insertSql = this.dataLakeSqlGenerator.generateInsertSql(table, rows);
        // insert some example data into the table
        final TableResult insertionResult =
                tableEnv.executeSql(insertSql);

        // since all cluster operations of the Table API are executed asynchronously,
        // we need to wait until the insertion has been completed,
        // an exception is thrown in case of an error
        insertionResult.await();
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
