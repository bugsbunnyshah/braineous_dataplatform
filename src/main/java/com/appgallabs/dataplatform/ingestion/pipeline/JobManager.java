package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.kafka.PropertiesHelper;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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

@ApplicationScoped
public class JobManager {
    private static final int TIME_OUT_MS = 30000;
    private static final int BATCH_SIZE = 10;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private Map<String,Long> pipeToOffset = new HashMap<>();

    public void submit(StreamExecutionEnvironment env, SecurityToken securityToken,
                       String driverConfiguration, String entity,
                       String pipeId, long offset){
        Long pipeOffset = this.pipeToOffset.get(pipeId);
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

        Properties props;
        KafkaConsumer<String, String> kafkaConsumer;
        TopicPartition tp = new TopicPartition(topic, 0);
        try {
            props = PropertiesHelper.getProperties();
            kafkaConsumer = new KafkaConsumer<>(props);
            kafkaConsumer.assign(Arrays.asList(tp));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        Thread t = new Thread(() -> {
            //keep running forever or until shutdown() is called from another thread.
            try {
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

                        if(jsonElement.isJsonArray()) {
                            JsonArray jsonArray = jsonElement.getAsJsonArray();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonObject inputJson = jsonArray.get(i).getAsJsonObject();
                                input.add(inputJson.toString());
                            }
                        }else if(jsonElement.isJsonObject()){
                            input.add(jsonElement.toString());
                        }

                        if(record.offset() == endOffset) {
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
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally{
                kafkaConsumer.close();
            }
        });
        t.start();
    }

    private synchronized void submitJob(StreamExecutionEnvironment env, List<String> input, SecurityToken securityToken,
                                        String driverConfiguration,
                                        String pipeId,
                                        String entity){
        try {
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
        }
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
