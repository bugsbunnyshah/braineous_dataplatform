package com.appgallabs.dataplatform.ingestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import static io.restassured.RestAssured.given;

public class IngestionAgent extends TimerTask implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(IngestionAgent.class);

    private Timer timer;
    private String entity;
    private DataFetchAgent dataFetchAgent;
    private DataPushAgent dataPushAgent;

    public IngestionAgent(String entity,DataFetchAgent dataFetchAgent){
        this.entity = entity;
        this.dataFetchAgent = dataFetchAgent;
    }

    public IngestionAgent(String entity,DataPushAgent dataPushAgent){
        this.entity = entity;
        this.dataPushAgent = dataPushAgent;
    }

    public void start(){
        this.timer = new Timer(true);
        this.timer.schedule(this, 1000, 15*60*1000);
    }

    @Override
    public void run() {
        try {
            JsonArray data = this.dataFetchAgent.fetchData();

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
            JsonObject input = new JsonObject();
            input.addProperty("sourceSchema", "");
            input.addProperty("destinationSchema", "");
            input.addProperty("sourceData", data.toString());
            input.addProperty("entity",entity);
            Response response = given().body(input.toString()).when().post("/dataMapper/map/")
                    .andReturn();
            response.getBody().prettyPrint();
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

    public void receiveData(JsonArray data){
        try {
            this.dataPushAgent.receiveData(data);

            JsonObject input = new JsonObject();
            input.addProperty("sourceSchema", "");
            input.addProperty("destinationSchema", "");
            input.addProperty("sourceData", data.toString());
            input.addProperty("entity",entity);
            Response response = given().body(input.toString()).when().post("/dataMapper/map/")
                    .andReturn();
            JsonObject ingestionResult = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
            //System.out.println("***************INGESTION_RESULT*******************");
            //System.out.println(ingestionResult);
            //System.out.println("**************************************************");
        }
        catch(Exception pushException){
            throw new RuntimeException(pushException);
        }
    }
}
