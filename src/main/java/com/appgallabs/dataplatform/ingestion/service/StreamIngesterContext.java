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
import org.apache.tinkerpop.gremlin.structure.Vertex;
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


    private StreamIngesterContext()
    {
        this.streamIngesterQueue = new StreamIngesterQueue();
        this.chainIds = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(1000);
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

    public void ingestData(String principal,String entity,String dataLakeId, String chainId, JsonObject jsonObject)
    {
        executorService.execute(() -> {
            this.ingestOnThread(principal,entity,dataLakeId,chainId,jsonObject);
        });
    }

    void ingestOnThread(String principal,String entity,String dataLakeId, String chainId, JsonObject jsonObject){
        //System.out.println("***INGESTING****");
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
            data.addProperty("tenant", tenant.getPrincipal());
            data.addProperty("entity",entity);
            data.addProperty("data", jsonObject.toString());
            data.addProperty("chainId", chainId);
            data.addProperty("dataLakeId", dataLakeId);
            data.addProperty("timestamp", ingestionTime.toEpochSecond());
            data.addProperty("objectHash", objectHash);
            if(!this.mongoDBJsonStore.entityExists(tenant,data)) {
                //System.out.println("****SAVING****");
                this.mongoDBJsonStore.storeIngestion(tenant, data);
                this.chainIds.put(dataLakeId, chainId);

                //Update the ObjectGraph service
                try {
                    this.queryService.saveObjectGraph(entity, jsonObject);
                }catch(Exception e){}


                BackgroundProcessListener.getInstance().decreaseThreshold(entity, dataLakeId, data);

                //TODO: Update DataHistory
                //data.remove("data");
                //this.mongoDBJsonStore.storeHistoryObject(tenant, data);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
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
