package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingArea;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.Debug;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.ehcache.sizeof.SizeOf;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreOrchestrator {

    private static StoreOrchestrator singleton = new StoreOrchestrator();

    private StagingArea stagingArea;

    ExecutorService threadpool = Executors.newCachedThreadPool();

    PerformanceReport performanceReport;


    private StoreOrchestrator(){
        this.performanceReport = new PerformanceReport();

        //find the staging area service
        this.stagingArea = CDI.current().select(StagingArea.class).get();
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
                            String pipeId, String entity, String data) {
        if(!this.performanceReport.started){
            this.performanceReport.started = true;
            this.performanceReport.start = System.currentTimeMillis();
        }else{
            this.performanceReport.counter++;
        }

        if(this.performanceReport.counter == 9999){
            this.performanceReport.end = System.currentTimeMillis();
            Debug.out("****PERFORMANCE_REPORT*****");
            Debug.out("PROCESSING_TIME: "+ this.performanceReport.toString());
            Debug.out("***************************");
        }else{
            Debug.out("***COUNTER****");
            Debug.out(""+this.performanceReport.counter);
        }


        String tenant = securityToken.getPrincipal();


        Registry registry = Registry.getInstance();

        //find the registered store drivers for this pipe
        List<StagingStore> registeredStores = registry.findStagingStores(tenant, pipeId);
        if(registeredStores == null || registeredStores.isEmpty()){
            return;
        }


        //TODO: make this transactional (GA)
        //fan out storage to each store
        /*registeredStores.parallelStream().forEach(stagingStore -> {
                JsonArray preStorageDataSet = JsonUtil.validateJson(data).getAsJsonArray();

                //adjust based on configured jsonpath expression (CR2)
                JsonArray mapped = this.mapDataSet(stagingStore, schemalessMapper,preStorageDataSet);

                this.storeDataToTarget(
                        securityToken,
                        stagingStore,
                        pipeId,
                        entity,
                        mapped);

                //TODO: (CR2)
                postProcess(securityToken,
                        systemStore,
                        storeDriver,
                        pipeId,
                        data
                        );
            }
        );*/

        for(StagingStore stagingStore: registeredStores){
            JsonArray preStorageDataSet = JsonUtil.validateJson(data).getAsJsonArray();

            //adjust based on configured jsonpath expression (CR2)
            JsonArray mapped = this.mapDataSet(stagingStore, schemalessMapper,preStorageDataSet);

            this.storeDataToTarget(
                    securityToken,
                    stagingStore,
                    pipeId,
                    entity,
                    mapped);
        }
    }

    private JsonArray mapDataSet(StagingStore stagingStore, SchemalessMapper schemalessMapper, JsonArray dataset){
        try {
            JsonArray mapped = new JsonArray();

            JsonObject configuration = stagingStore.getConfiguration();
            if (!configuration.has("jsonpathExpressions")) {
                return dataset;
            }


            JsonArray jsonPathExpressions = configuration.getAsJsonArray("jsonpathExpressions");
            if(jsonPathExpressions.size() == 0) {
                return dataset;
            }

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

    private void storeDataToTarget(SecurityToken securityToken, StagingStore stagingStore,
                                   String pipeId,
                                   String entity,
                                   JsonArray mapped){

        final String data = mapped.toString();

        /*this.threadpool.execute(() -> {
            List<Record> records = stagingArea.receiveDataForStorage(
                    securityToken,
                    stagingStore,
                    pipeId,
                    entity,
                    data);

            this.stagingArea.runIntegrationAgent(
                    securityToken,
                    stagingStore,
                    pipeId,
                    entity,
                    records);
        });*/

        List<Record> records = stagingArea.receiveDataForStorage(
                securityToken,
                stagingStore,
                pipeId,
                entity,
                data);

        this.stagingArea.runIntegrationAgent(
                securityToken,
                stagingStore,
                pipeId,
                entity,
                records);
    }

    private void postProcess(SecurityToken securityToken, SystemStore systemStore,
                             StagingStore stagingStore,
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

        //TODO: (CR2)
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("targetSystem", stagingStore.getName());
        jsonObject.addProperty("pipeId", pipeId);
        jsonObject.addProperty("message", data);
        jsonObject.addProperty("sizeInBytes", dataStreamSize);
        jsonObject.addProperty("outgoing", true);

        collection.insertOne(Document.parse(jsonObject.toString()));
    }
}
