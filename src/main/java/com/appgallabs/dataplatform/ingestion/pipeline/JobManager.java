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

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import org.bson.Document;
import org.ehcache.sizeof.SizeOf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class JobManager {
    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SchemalessMapper schemalessMapper;

    private Map<String,Long> pipeToOffset = new HashMap<>();

    private ExecutorService submitJobPool = Executors.newFixedThreadPool(25);
    private ExecutorService retryJobPool = Executors.newFixedThreadPool(25);

    public synchronized void submit(StreamExecutionEnvironment env, SecurityToken securityToken,
                       String driverConfiguration, String entity,
                       String pipeId, long offset, String jsonString){
        JsonElement jsonElement = JsonParser.parseString(jsonString);

        JsonArray ingestion = new JsonArray();
        if (jsonElement.isJsonArray()) {
            ingestion = jsonElement.getAsJsonArray();
        } else if (jsonElement.isJsonObject()) {
            ingestion.add(jsonElement);
        }

        //ingestion array
        List<Map<String, Object>> flatArray = new ArrayList();
        for(int i=0; i<ingestion.size(); i++) {
            JsonObject jsonObject = ingestion.get(i).getAsJsonObject();
            Map<String, Object> flatJson = this.schemalessMapper.mapAll(jsonObject.toString());
            flatArray.add(flatJson);
        }


        submitJob(env,
                flatArray,
                securityToken,
                driverConfiguration,
                pipeId,
                entity);
    }

    private synchronized void submitJob(StreamExecutionEnvironment env, List<Map<String, Object>> flatArray, SecurityToken securityToken,
                                        String driverConfiguration,
                                        String pipeId,
                                        String entity){
        submitJobPool.execute(() -> {
            while(true){
                boolean success = submitJob(env,flatArray);
                if(!success){
                    retryJob(env,flatArray);
                }
                break;
            }
        });
    }

    private void retryJob(StreamExecutionEnvironment env,List<Map<String, Object>> flatArray){
        retryJobPool.execute(() -> {
            while(true){
                boolean success = submitJob(env,flatArray);
                if(success){
                    break;
                }
            }
            System.out.println("******RETRY_WAS_SUCCESSFULL******");
        });
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
