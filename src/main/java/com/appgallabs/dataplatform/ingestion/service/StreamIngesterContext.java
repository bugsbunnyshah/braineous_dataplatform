package com.appgallabs.dataplatform.ingestion.service;


import com.appgallabs.dataplatform.history.service.DataReplayService;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.EntityCallback;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.appgallabs.dataplatform.util.BackgroundProcessListener;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StreamIngesterContext implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(StreamIngesterContext.class);

    private static StreamIngester streamIngester;
    private static StreamIngesterContext streamIngesterContext;

    private StreamIngesterQueue streamIngesterQueue;

    private MongoDBJsonStore mongoDBJsonStore;

    private DataReplayService dataReplayService;

    private SecurityTokenContainer securityTokenContainer;

    private Map<String,String> chainIds;

    private ObjectGraphQueryService queryService;

    private ExecutorService executorService;

    private IngestionService ingestionService;

    private Map<String, EntityCallback> callbackMap = new HashMap<>();


    private Map<String,Integer> batchTracker = new HashedMap();


    private StreamIngesterContext()
    {
        try {
            this.streamIngesterQueue = new StreamIngesterQueue();
            this.chainIds = new HashMap<>();

            //load callbacks
            String configJsonString = IOUtils.toString(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("entityCallbacks.json"),
                    StandardCharsets.UTF_8
            );
            JsonArray configJson = JsonParser.parseString(configJsonString).getAsJsonArray();
            Iterator<JsonElement> iterator = configJson.iterator();
            while (iterator.hasNext()) {
                JsonObject entityConfigJson = iterator.next().getAsJsonObject();
                String entity = entityConfigJson.get("entity").getAsString();
                String callback = entityConfigJson.get("callback").getAsString();
                EntityCallback object = (EntityCallback) Thread.currentThread().getContextClassLoader().
                        loadClass(callback).getDeclaredConstructor().newInstance();
                this.callbackMap.put(entity, object);
            }

            this.executorService = Executors.newFixedThreadPool(1000);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void announce(){
        logger.info("****************");
        logger.info("STREAM_INGESTION_IS_ACTIVE....");
        logger.info("****************");
    }

    public static StreamIngester getStreamIngester()
    {
        if(StreamIngesterContext.streamIngester == null){
            getStreamIngesterContext().announce();
            StreamIngesterContext.streamIngester = new StreamIngester();
            StreamIngesterContext.streamIngester.start();
        }
        return StreamIngesterContext.streamIngester;
    }

    public static StreamIngesterContext getStreamIngesterContext()
    {
        if(StreamIngesterContext.streamIngesterContext == null){
            StreamIngesterContext.streamIngesterContext = new StreamIngesterContext();
        }
        return StreamIngesterContext.streamIngesterContext;
    }

    public IngestionService getIngestionService() {
        return ingestionService;
    }

    public void setIngestionService(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    public void clear(){
        StreamIngesterContext.streamIngester = null;
        StreamIngesterContext.streamIngesterContext = null;
    }

    public void addStreamObject(StreamObject streamObject)
    {
        this.streamIngesterQueue.add(streamObject);
    }

    public Queue<StreamObject> getDataLakeQueue(String dataLakeId){
        return this.streamIngesterQueue.getDataLakeQueue(dataLakeId);
    }

    public Set<String> activeDataLakeIds()
    {
        return this.streamIngesterQueue.getActiveDataLakeIds();
    }

    public Map<String, String> getChainIds() {
        return chainIds;
    }

    public void ingestData(String principal,String entity,String dataLakeId, String chainId, int batchSize, JsonObject jsonObject)
    {
        executorService.execute(() -> {
            this.ingestOnThread(principal,entity,dataLakeId,chainId,batchSize,jsonObject);
        });
    }

    void ingestOnThread(String principal,String entity,String dataLakeId, String chainId, int batchSize, JsonObject jsonObject){
        System.out.println("***INGESTING****");
        try {
            Tenant tenant = new Tenant();
            tenant.setPrincipal(principal);
            SecurityToken securityToken = new SecurityToken();
            securityToken.setPrincipal(principal);
            this.securityTokenContainer.setSecurityToken(securityToken);
            System.out.println("***1****");

            OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
            String objectHash = JsonUtil.getJsonHash(jsonObject);
            jsonObject.addProperty("objectHash",objectHash);
            System.out.println("***2****");

            JsonObject data = new JsonObject();
            data.addProperty("braineous_datalakeid", dataLakeId);
            data.addProperty("batchSize", batchSize);
            data.addProperty("tenant", tenant.getPrincipal());
            data.addProperty("entity",entity);
            data.addProperty("data", jsonObject.toString());
            data.addProperty("chainId", chainId);
            data.addProperty("dataLakeId", dataLakeId);
            data.addProperty("timestamp", ingestionTime.toEpochSecond());
            data.addProperty("objectHash", objectHash);
            System.out.println("***3****");

            this.storeToDataLake(tenant,data);
            this.storeToGraph(tenant,entity,data);

            this.notifyBatchTracker(batchSize,chainId,entity,data);


            BackgroundProcessListener.getInstance().decreaseThreshold(entity, dataLakeId, data);

            //TODO: Update DataHistory
            //data.remove("data");
            //this.mongoDBJsonStore.storeHistoryObject(tenant, data);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
    }

    private synchronized void notifyBatchTracker(int batchSize,String chainId,String entity,JsonObject data){
        Integer currentSize = this.batchTracker.get(chainId);
        System.out.println("BTACH: "+currentSize);
        if(currentSize == null){
            this.batchTracker.put(chainId,1);
            System.out.println("****TRACKER_STARTED********: "+this.batchTracker.get(chainId));
        }else{
            int newCount = currentSize + 1;
            System.out.println("******NEW_COUNT******"+newCount);
            this.batchTracker.put(chainId,newCount);
            System.out.println("****TRACKER_UPDATED********: "+this.batchTracker.get(chainId));

            if(newCount == batchSize) {
                this.issueCallback(currentSize, chainId, entity, data);
            }
        }
    }

    private void issueCallback(int batchSize,String chainId,String entity,JsonObject data){
        System.out.println("**************************ISSUING_CALLBACK*********************************************");
        EntityCallback callback = this.callbackMap.get(entity);
        if (callback != null) {
            callback.call(this.queryService, entity, data);
        }
    }

    private void storeToDataLake(Tenant tenant,JsonObject data){
        if (!this.mongoDBJsonStore.entityExists(tenant, data)) {
            this.mongoDBJsonStore.storeIngestion(tenant, data);
            //this.chainIds.put(dataLakeId, chainId);
            System.out.println("****SAVED_TO_MONGO_DB****");
        }
    }

    private void storeToGraph(Tenant tenant,String entity,JsonObject jsonObject){
        //if (!this.mongoDBJsonStore.entityExists(tenant, data)) {
            System.out.println("****SAVING_TO_NEO****");
            this.queryService.saveObjectGraph(entity, jsonObject);
            System.out.println("****SAVING_TO_NEO_SUCCESS****");
        //}
    }

    public void setQueryService(ObjectGraphQueryService queryService) {
        this.queryService = queryService;
    }

    public void setDataReplayService(DataReplayService dataReplayService){
        this.dataReplayService = dataReplayService;
    }

    public void setMongoDBJsonStore(MongoDBJsonStore mongoDBJsonStore) {
        this.mongoDBJsonStore = mongoDBJsonStore;
    }

    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }
}
