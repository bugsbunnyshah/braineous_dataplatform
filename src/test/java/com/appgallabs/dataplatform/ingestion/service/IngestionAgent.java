package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;


public class IngestionAgent extends TimerTask implements Serializable,FetchAgent,PushAgent {
    private static Logger logger = LoggerFactory.getLogger(IngestionAgent.class);

    private Timer timer;
    private String entity;
    private MapperService mapperService;
    private Tenant tenant;
    private SecurityTokenContainer securityTokenContainer;
    private MongoDBJsonStore mongoDBJsonStore;

    public IngestionAgent() {
    }

    @Override
    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }

    @Override
    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public void setMapperService(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @Override
    public void setMongoDBJsonStore(MongoDBJsonStore mongoDBJsonStore) {
        this.mongoDBJsonStore = mongoDBJsonStore;
    }

    public void receiveData(JsonArray data){
        try {
            /*this.receiveData(data);

            JsonObject input = new JsonObject();
            input.addProperty("sourceSchema", "");
            input.addProperty("destinationSchema", "");
            input.addProperty("sourceData", data.toString());
            input.addProperty("entity",entity);
            //Response response = given().body(input.toString()).when().post("/dataMapper/map/")
            //        .andReturn();
            //JsonObject ingestionResult = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
            //System.out.println("***************INGESTION_RESULT*******************");
            //System.out.println(ingestionResult);
            //System.out.println("**************************************************");*/
        }
        catch(Exception pushException){
            throw new RuntimeException(pushException);
        }
    }

    @Override
    public void startFetch() {
        this.timer = new Timer(true);
        this.timer.schedule(this, 1000, 15*60*1000);
    }

    @Override
    public boolean isStarted() {
        if(this.entity == null) {
            return false;
        }
        return true;
    }

    @Override
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    @Override
    public void run() {
        try {
            //TODO: investigate the Security Aspect and Networking Aspect
            /*JsonObject json = new JsonObject();
            json.addProperty("sourceSchema", "");
            json.addProperty("destinationSchema", "");
            json.addProperty("sourceData", data.toString());
            json.addProperty("entity",this.entity);


            String restUrl = "http://127.0.0.1:8080/dataMapper/map/";
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();

            JsonObject ingestionResult = JsonParser.parseString(responseJson).getAsJsonObject();
            System.out.println("***************INGESTION_RESULT*******************");
            System.out.println(ingestionResult);
            System.out.println("**************************************************");*/
            /*JsonObject input = new JsonObject();
            input.addProperty("sourceSchema", "");
            input.addProperty("destinationSchema", "");
            input.addProperty("sourceData", data.toString());
            input.addProperty("entity",entity);*/
            //Response response = given().body(input.toString()).when().post("/dataMapper/map/")
            //        .andReturn();
            //response.getBody().prettyPrint();
            //JsonObject ingestionResult = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
            //System.out.println("***************INGESTION_RESULT*******************");
            //System.out.println(ingestionResult);
            //System.out.println("**************************************************");
        }
        catch (Exception fetchException)
        {
            logger.error(fetchException.getMessage(),fetchException);
        }
    }
}
