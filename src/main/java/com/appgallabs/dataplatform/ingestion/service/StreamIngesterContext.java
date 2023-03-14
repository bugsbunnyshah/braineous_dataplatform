package com.appgallabs.dataplatform.ingestion.service;


import com.appgallabs.dataplatform.history.service.DataReplayService;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.appgallabs.dataplatform.util.BackgroundProcessListener;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
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

    private CallbackAgent callbackAgent;

    private String environment;


    private StreamIngesterContext()
    {
        try {
            this.streamIngesterQueue = new StreamIngesterQueue();
            this.chainIds = new HashMap<>();
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

    public void setQueryService(ObjectGraphQueryService queryService) {
        this.queryService = queryService;
        this.callbackAgent = new CallbackAgent(this.environment,this.queryService
        ,this.mongoDBJsonStore,this.securityTokenContainer);
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

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
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
        try {
            Tenant tenant = new Tenant();
            tenant.setPrincipal(principal);
            SecurityToken securityToken = new SecurityToken();
            securityToken.setPrincipal(principal);
            this.securityTokenContainer.setSecurityToken(securityToken);

            OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
            String objectHash = JsonUtil.getJsonHash(jsonObject);
            jsonObject.addProperty("objectHash",objectHash);

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

            this.storeToDataLake(tenant,data);
            //this.storeToGraph(tenant,entity,jsonObject);

            this.callbackAgent.notifyBatchTracker(batchSize,chainId,entity,data);


            //BackgroundProcessListener.getInstance().decreaseThreshold(entity, dataLakeId, data);

            //TODO: Update DataHistory
            //data.remove("data");
            //this.mongoDBJsonStore.storeHistoryObject(tenant, data);
        }
        catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage(),e);
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
        this.queryService.saveObjectGraph(entity, jsonObject);
    }
}
