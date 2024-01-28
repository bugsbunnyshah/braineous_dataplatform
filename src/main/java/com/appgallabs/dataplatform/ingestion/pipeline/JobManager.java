package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.kafka.PropertiesHelper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.examples.java.basics.StreamSQLExample;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.ehcache.sizeof.SizeOf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class JobManager {
    private static final int TIME_OUT_MS = 30000;
    private static final int BATCH_SIZE = 10;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private Map<String,Long> pipeToOffset = new HashMap<>();

    private ExecutorService submitJobPool = Executors.newFixedThreadPool(25);
    private ExecutorService retryJobPool = Executors.newFixedThreadPool(25);

    public synchronized void submit(StreamExecutionEnvironment env, SecurityToken securityToken,
                       String driverConfiguration, String entity,
                       String pipeId, long offset, String jsonString){
        List<String> input = new ArrayList<>();
        JsonElement jsonElement = JsonParser.parseString(jsonString);

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject inputJson = jsonArray.get(i).getAsJsonObject();
                input.add(inputJson.toString());
            }
        } else if (jsonElement.isJsonObject()) {
            input.add(jsonElement.toString());
        }

        JsonUtil.printStdOut(jsonElement);

        submitJob(env,
                input,
                securityToken,
                driverConfiguration,
                pipeId,
                entity);

        /*Long pipeOffset = this.pipeToOffset.get(pipeId);
        if(pipeOffset == null){
            this.pipeToOffset.put(pipeId, offset);
            return;
        }

        if(offset - pipeOffset < BATCH_SIZE){
            return;
        }

        long startOffset = pipeOffset;
        long endOffset = offset;
        String topic = pipeId;
        this.pipeToOffset.put(pipeId, offset);

        Thread t = new Thread(() -> {
            KafkaConsumer<String, String> kafkaConsumer = null;
            try {
                Properties props;
                TopicPartition tp = new TopicPartition(topic, 0);
                try {
                    props = PropertiesHelper.getProperties();
                    kafkaConsumer = new KafkaConsumer<>(props);
                    kafkaConsumer.assign(Arrays.asList(tp));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                kafkaConsumer.seek(tp, startOffset);

                boolean exit = false;
                List<String> input = new ArrayList<>();
                while (!exit) {
                    ConsumerRecords<String, String> records =
                            kafkaConsumer.poll(Duration.ofMillis(TIME_OUT_MS));
                    if (records.count() == 0) {
                        continue;
                    }

                    for (ConsumerRecord<String, String> record : records) {
                        String messageValue = record.value();
                        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();
                        String payload = json.get("message").getAsString();
                        JsonElement payloadElem = JsonParser.parseString(payload);
                        String jsonString = payloadElem.toString();
                        JsonElement jsonElement = JsonParser.parseString(jsonString);

                        if (jsonElement.isJsonArray()) {
                            JsonArray jsonArray = jsonElement.getAsJsonArray();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonObject inputJson = jsonArray.get(i).getAsJsonObject();
                                input.add(inputJson.toString());
                            }
                        } else if (jsonElement.isJsonObject()) {
                            input.add(jsonElement.toString());
                        }

                        if (record.offset() == endOffset) {
                            exit = true;
                        }
                    }
                }

                submitJob(env,
                        input,
                        securityToken,
                        driverConfiguration,
                        pipeId,
                        entity);
            }finally {
                kafkaConsumer.close();
            }
        });
        t.start();*/
    }

    private synchronized void submitJob(StreamExecutionEnvironment env, List<String> input, SecurityToken securityToken,
                                        String driverConfiguration,
                                        String pipeId,
                                        String entity){
        /*try {
            System.out.println("****NUM_OF_RECORDS****");
            System.out.println(input.size());
            System.out.println("**********************");

            //pre-process
            for(String entry:input){
                this.preProcess(entry, securityToken, pipeId);
            }

            SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();
            DataLakeSinkFunction sinkFunction = new DataLakeSinkFunction(securityToken,
                    systemStore,
                    driverConfiguration,
                    pipeId,
                    entity);

            DataStream<String> dataEvents = env.fromCollection(input);

            dataEvents.addSink(sinkFunction);
            env.execute();
        }catch(Exception e){
            throw new RuntimeException(e);
        }*/
        submitJobPool.execute(() -> {
            while(true){
                boolean success = submitJob(env);
                if(!success){
                    retryJob(env);
                }
                break;
            }
        });
    }

    private void retryJob(StreamExecutionEnvironment env){
        retryJobPool.execute(() -> {
            while(true){
                boolean success = submitJob(env);
                if(success){
                    break;
                }
            }
            System.out.println("******RETRY_WAS_SUCCESSFULL******");
        });
    }

    private boolean submitJob(StreamExecutionEnvironment env)
    {
        // set up the Java Table API
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        final DataStream<StreamSQLExample.Order> orderA =
                env.fromCollection(
                        Arrays.asList(
                                new StreamSQLExample.Order(1L, "beer1", 3),
                                new StreamSQLExample.Order(1L, "diaper2", 4),
                                new StreamSQLExample.Order(3L, "rubber3", 2)));

        final DataStream<StreamSQLExample.Order> orderB =
                env.fromCollection(
                        Arrays.asList(
                                new StreamSQLExample.Order(2L, "pen", 3),
                                new StreamSQLExample.Order(2L, "rubber", 3),
                                new StreamSQLExample.Order(4L, "beer", 1)));

        // convert the first DataStream to a Table object
        // it will be used "inline" and is not registered in a catalog
        final Table tableA = tableEnv.fromDataStream(orderA);

        String sql = "SELECT * FROM "
                + tableA;

        final Table result =
                tableEnv.sqlQuery(sql);

        tableEnv.toDataStream(result, StreamSQLExample.Order.class).print();

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
