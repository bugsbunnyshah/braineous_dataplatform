package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.Debug;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.bson.Document;
import org.ehcache.sizeof.SizeOf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class StoreOrchestrator {

    private static StoreOrchestrator singleton = new StoreOrchestrator();


    private StoreOrchestrator(){
    }

    public static StoreOrchestrator getInstance(){
        //safe-check, cause why not
        if(StoreOrchestrator.singleton == null){
            StoreOrchestrator.singleton = new StoreOrchestrator();
        }
        return StoreOrchestrator.singleton;
    }

    public void receiveData(SecurityToken securityToken,
                            SystemStore systemStore,
                            SchemalessMapper schemalessMapper,
                            String pipeId, String data) {
        String tenant = securityToken.getPrincipal();

        Debug.out("******STORE_ORCHESTRATOR********");
        Debug.out("PipeId: "+pipeId);
        Debug.out("Data: "+data);
        Debug.out("*******************************&");

        Registry registry = Registry.getInstance();

        //find the registered store drivers for this pipe
        List<StoreDriver> storeDrivers = registry.findStoreDrivers(tenant, pipeId);
        if(storeDrivers == null || storeDrivers.isEmpty()){
            return;
        }


        //TODO: make this transactional (GA)
        //fan out storage to each store
        storeDrivers.stream().forEach(storeDriver -> {
                JsonArray preStorageDataSet = JsonUtil.validateJson(data).getAsJsonArray();

                //adjust based on configured jsonpath expression (CR2)
                JsonArray mapped = this.mapDataSet(storeDriver, schemalessMapper,preStorageDataSet);

                storeDriver.storeData(mapped);

                postProcess(securityToken,
                        systemStore,
                        storeDriver,
                        pipeId,
                        data
                        );
            }
        );
    }

    private JsonArray mapDataSet(StoreDriver storeDriver, SchemalessMapper schemalessMapper, JsonArray dataset){
        try {
            JsonArray mapped = new JsonArray();

            JsonObject configuration = storeDriver.getConfiguration();
            if (!configuration.has("jsonpathExpressions")) {
                return dataset;
            }


            JsonArray jsonPathExpressions = configuration.getAsJsonArray("jsonpathExpressions");

            List<String> queries = new ArrayList<>();
            for (int i = 0; i < jsonPathExpressions.size(); i++) {
                String jsonPathExpression = jsonPathExpressions.get(i).getAsString();
                queries.add(jsonPathExpression);
            }

            for (int i = 0; i < dataset.size(); i++) {
                JsonObject datasetElement = dataset.get(i).getAsJsonObject();
                JsonObject ingestedJson = schemalessMapper.mapSubsetDataset(datasetElement.toString(), queries);
                mapped.add(ingestedJson);
            }

            return mapped;
        }catch(Exception e){
            return dataset;
        }
    }

    private void postProcess(SecurityToken securityToken, SystemStore systemStore,
                             StoreDriver storeDriver,
                             String pipeId,
                             String data){
        String principal = securityToken.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        //setup driver components
        MongoClient mongoClient = systemStore.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = db.getCollection("pipeline_monitoring");

        Queue<String> queue = new LinkedList<>();
        queue.add(data);
        SizeOf sizeOf = SizeOf.newInstance();
        long dataStreamSize = sizeOf.deepSizeOf(queue);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("targetSystem", storeDriver.getName());
        jsonObject.addProperty("pipeId", pipeId);
        jsonObject.addProperty("message", data);
        jsonObject.addProperty("sizeInBytes", dataStreamSize);
        jsonObject.addProperty("outgoing", true);

        collection.insertOne(Document.parse(jsonObject.toString()));
    }
}
