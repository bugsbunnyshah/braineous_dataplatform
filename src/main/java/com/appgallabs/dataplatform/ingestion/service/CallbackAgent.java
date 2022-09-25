package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.EntityCallback;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CallbackAgent {
    private static Logger logger = LoggerFactory.getLogger(CallbackAgent.class);

    private Map<String,Integer> batchTracker = new HashedMap();
    private Map<String, EntityCallback> callbackMap = new HashMap<>();

    private ObjectGraphQueryService queryService;

    private MongoDBJsonStore mongoDBJsonStore;

    private SecurityTokenContainer securityTokenContainer;

    public CallbackAgent(ObjectGraphQueryService queryService,
                         MongoDBJsonStore mongoDBJsonStore,
                         SecurityTokenContainer securityTokenContainer){
        try {
            this.mongoDBJsonStore = mongoDBJsonStore;
            this.securityTokenContainer = securityTokenContainer;
            this.queryService = queryService;

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
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public synchronized void notifyBatchTracker(int batchSize,String chainId,String entity,JsonObject data){
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
        String dataLakeId = data.get("dataLakeId").getAsString();
        String tenant = this.securityTokenContainer.getTenant().getPrincipal();
        System.out.println(dataLakeId);
        System.out.println(tenant);
        JsonArray array = this.mongoDBJsonStore.getIngestion(
                this.securityTokenContainer.getTenant(),
                dataLakeId);
        JsonUtil.printStdOut(array);
        EntityCallback callback = this.callbackMap.get(entity);
        if (callback != null) {
            callback.call(this.queryService, entity, array);
        }
    }
}
