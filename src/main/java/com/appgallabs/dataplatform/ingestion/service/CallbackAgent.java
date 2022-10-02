package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CallbackAgent {
    private static Logger logger = LoggerFactory.getLogger(CallbackAgent.class);

    private Map<String,Integer> batchTracker = new HashedMap();

    Map<String,String> callbackMap = new HashMap<>();

    private ObjectGraphQueryService queryService;

    private MongoDBJsonStore mongoDBJsonStore;

    private SecurityTokenContainer securityTokenContainer;

    private HttpClient httpClient = HttpClient.newBuilder().build();

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
                this.callbackMap.put(entity,callback);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public synchronized void notifyBatchTracker(int batchSize,String chainId,String entity,JsonObject data){
        Integer currentSize = this.batchTracker.get(chainId);
        if(currentSize == null){
            this.batchTracker.put(chainId,1);
        }else{
            int newCount = currentSize + 1;
            this.batchTracker.put(chainId,newCount);
            if(newCount == batchSize) {
                this.issueCallback(entity, data);
            }
        }
    }

    private void issueCallback(String entity,JsonObject data){
        System.out.println("**************************ISSUING_CALLBACK*********************************************");
        String dataLakeId = data.get("dataLakeId").getAsString();
        String tenant = this.securityTokenContainer.getTenant().getPrincipal();
        System.out.println(dataLakeId);
        System.out.println(tenant);
        JsonArray array = this.mongoDBJsonStore.getIngestion(
                this.securityTokenContainer.getTenant(),
                dataLakeId);
        String callback = this.callbackMap.get(entity);
        this.makeCall(callback, entity, array);
    }

    private void makeCall(String restUrl,String entity,JsonArray array){
        try {
            System.out.println("********CALLBACK+++********************");
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(array.toString()))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
