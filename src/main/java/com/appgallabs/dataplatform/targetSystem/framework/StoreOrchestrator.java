package com.appgallabs.dataplatform.targetSystem.framework;

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

import java.util.List;

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

    public void receiveData(SecurityToken securityToken, SystemStore systemStore, String pipeId, String data) {
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

                //TODO: adjust based on configured jsonpath expression (CR2)

                storeDriver.storeData(preStorageDataSet);

                postProcess(securityToken,
                        systemStore,
                        storeDriver,
                        pipeId,
                        data
                        );
            }
        );
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

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("shivji", "isBossman");
        jsonObject.addProperty("targetSystem", storeDriver.getName());
        jsonObject.addProperty("pipeId", pipeId);
        jsonObject.addProperty("message", data);
        jsonObject.addProperty("outgoing", true);

        collection.insertOne(Document.parse(jsonObject.toString()));
    }
}
