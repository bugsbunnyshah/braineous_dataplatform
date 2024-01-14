package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.Debug;
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
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.bson.Document;
import org.ehcache.sizeof.SizeOf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreOrchestrator {

    private static StoreOrchestrator singleton = new StoreOrchestrator();

    ExecutorService threadpool = Executors.newCachedThreadPool();

    PerformanceReport performanceReport;


    private StoreOrchestrator(){
        this.performanceReport = new PerformanceReport();
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
                            String flinkHost,
                            String flinkPort,
                            SchemalessMapper schemalessMapper,
                            String pipeId, String data) {
        if(!this.performanceReport.started){
            this.performanceReport.started = true;
            this.performanceReport.start = System.currentTimeMillis();
        }else{
            this.performanceReport.counter++;
        }

        if(this.performanceReport.counter == 999){
            this.performanceReport.end = System.currentTimeMillis();
            Debug.out("****PERFORMANCE_REPORT*****");
            Debug.out("PROCESSING_TIME: "+ this.performanceReport.toString());
            Debug.out("***************************");
        }else{
            Debug.out("***COUNTER****");
            Debug.out(""+this.performanceReport.counter);
        }


        String tenant = securityToken.getPrincipal();

        /*Debug.out("******STORE_ORCHESTRATOR********");
        Debug.out("PipeId: "+pipeId);
        Debug.out("Data: "+data);
        Debug.out("*******************************&");*/

        Registry registry = Registry.getInstance();

        //find the registered store drivers for this pipe
        List<StoreDriver> storeDrivers = registry.findStoreDrivers(tenant, pipeId);
        if(storeDrivers == null || storeDrivers.isEmpty()){
            return;
        }


        //TODO: make this transactional (GA)
        //fan out storage to each store
        storeDrivers.parallelStream().forEach(storeDriver -> {
                /*System.out.println("****DEBUG******");
                JsonUtil.printStdOut(storeDriver.getConfiguration());
                System.out.println("***************");*/

                JsonArray preStorageDataSet = JsonUtil.validateJson(data).getAsJsonArray();

                //adjust based on configured jsonpath expression (CR2)
                JsonArray mapped = this.mapDataSet(storeDriver, schemalessMapper,preStorageDataSet);

                this.storeDataToTarget(flinkHost,
                        flinkPort,
                        storeDriver,
                        mapped);

                //TODO: (CR2)
                /*postProcess(securityToken,
                        systemStore,
                        storeDriver,
                        pipeId,
                        data
                        );*/
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

    private void storeDataToTarget(String flinkHost,
                                   String flinkPort,
                                   StoreDriver storeDriver,
                                   JsonArray mapped){
        /*try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                    flinkHost,
                    Integer.parseInt(flinkPort),
                    "dataplatform-1.0.0-cr2-runner.jar"
            );

            List<String> input = new ArrayList<>();
            for (int i = 0; i < mapped.size(); i++) {
                JsonObject inputJson = mapped.get(i).getAsJsonObject();
                input.add(inputJson.toString());
            }

            DataStream<String> dataEvents = env.fromCollection(input);

            StoreSinkFunction storeSinkFunction = new StoreSinkFunction(storeDriver);

            dataEvents.addSink(storeSinkFunction);

            env.execute();
        }catch(Exception e){
            e.printStackTrace();
        }*/
        this.threadpool.execute(() -> {
            //Debug.out("****STORE_SINK_FUNCTION*****");
            //Debug.out(value);
            //Debug.out("***************************");

            //TODO:
            //MySqlStoreDriver mySqlStoreDriver = new MySqlStoreDriver();
            //mySqlStoreDriver.storeData(data.toString());

            storeDriver.storeData(mapped);
        });
    }
}
