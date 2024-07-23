package com.appgallabs.dataplatform.targetSystem.core.driver;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.reporting.IngestionReportingService;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.RecordGenerator;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.*;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ElasticSearchStagingStore implements StagingStore {
    private static Logger logger = LoggerFactory.getLogger(ElasticSearchStagingStore.class);

    private JsonObject configJson;

    private String elasticSearchUrl;

    private String index;

    private JsonObject security;

    //TODO: (NOW) - thread it in
    private IngestionReportingService ingestionReportingService;



    @Override
    public void configure(JsonObject configJson) {
        this.configJson = configJson;

        //Set ElasticSearch Configuration
        this.elasticSearchUrl = this.configJson.get("elasticSearchUrl").getAsString();
        this.index = this.configJson.get("index").getAsString();
        this.security = this.configJson.get("security").getAsJsonObject();
    }

    @Override
    public String getName() {
        return this.configJson.get("connectionString").getAsString();
    }

    @Override
    public JsonObject getConfiguration() {
        return this.configJson;
    }

    @Override
    public void storeData(Tenant tenant, String pipeId, String entity, List<Record> records) {
        JsonArray dataSet = new JsonArray();
        for(Record record: records){
            JsonObject data = record.getData();
            dataSet.add(data);
        }
        this.storeData(dataSet);
    }

    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        try {
            JsonArray data = this.readData(tenant, pipeId, entity);

            RecordGenerator recordGenerator = new RecordGenerator();
            long offset = 0l;
            List<Record> records = recordGenerator.parsePayload(
                    tenant,
                    pipeId,
                    offset,
                    entity,
                    data.toString()
            );

            return records;
        }catch(Exception e){
            logger.error(e.getMessage());

            //report to the pipeline monitoring service
            JsonObject jsonObject = new JsonObject();
            this.ingestionReportingService.reportDataError(jsonObject);

            throw new RuntimeException(e);
        }
    }

    //------------------------------------------------------------------------------------
    private void storeData(JsonArray dataSet) {
        try {
            String bulkPostUrl = this.elasticSearchUrl+"/_bulk?pretty";

            //Security
            String type = this.security.get("type").getAsString();

            if(!type.equals("built_in_users")){
                throw new RuntimeException("Only 'built_in_users' authentication is supported currently." +
                        "Support for more authentication methods will be added in a future release");
            }

            String username = this.security.get("user").getAsString();
            String password = this.security.get("password").getAsString();

            JsonObject indexJsonComplete = new JsonObject();
            JsonObject indexJson = new JsonObject();
            indexJson.addProperty("_index", this.index);
            indexJsonComplete.add("index", indexJson);
            String indexJsonString = indexJsonComplete.toString();
            StringBuilder payloadBuilder = new StringBuilder();
            for(int i=0; i<dataSet.size();i++){
                JsonObject data = dataSet.get(i).getAsJsonObject();
                String dataString = data.toString();

                payloadBuilder.append(indexJsonString+"\n");
                payloadBuilder.append(dataString+"\n");
            }

            String payloadString = payloadBuilder.toString();

            //send request
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(bulkPostUrl))
                    .header("Authorization", basicAuth(username, password))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            //Handle logging
            int statusCode = httpResponse.statusCode();
            String result = httpResponse.body();

            JsonObject resultJson = new JsonObject();
            resultJson.addProperty("status_code", statusCode);
            resultJson.add("response", JsonUtil.validateJson(result));
            JsonUtil.printStdOut(resultJson);



        }catch(Exception e){
            logger.error(e.getMessage());

            //report to the pipeline monitoring service
            //JsonObject jsonObject = new JsonObject();
            //this.ingestionReportingService.reportDataError(jsonObject);
        }
    }

    private JsonArray readData(Tenant tenant, String pipeId, String entity){
        return new JsonArray();
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
