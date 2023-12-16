package com.appgallabs.dataplatform.client.sdk.network;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DataPipelineClient {
    private static DataPipelineClient singleton = new DataPipelineClient();

    private DataPipelineClient(){

    }

    public static DataPipelineClient getInstance(){
        //safe-check, cause why not
        if(DataPipelineClient.singleton == null){
            DataPipelineClient.singleton = new DataPipelineClient();
        }
        return DataPipelineClient.singleton;
    }

    public JsonObject sendData(String pipeId, String entity, JsonElement jsonElement){
        System.out.println("***SENDING_DATA_NETWORK*****");
        try {
            Configuration configuration = DataPipeline.getConfiguration();
            String baseUrl = configuration.ingestionHostUrl();
            String restUrl = baseUrl+"ingestion/json/";

            String payload = jsonElement.toString();

            //get apikey
            String apiKey = configuration.getApiKey();

            //get apiSecret
            String apiSecret = configuration.getApiSecret();

            //provide response
            JsonObject response = this.handleRestCallForSendData(restUrl,apiKey,apiSecret,
                    pipeId, entity, payload);
            response.addProperty("ingestionStatusCode", response.get("httpResponseCode").getAsString());

            return response;
        }catch(Exception e){
            JsonObject error = new JsonObject();
            error.addProperty("ingestionError",e.getMessage());
            return error;
        }
    }

    public JsonObject registerPipe(JsonElement jsonElement){
        try {
            Configuration configuration = DataPipeline.getConfiguration();
            String baseUrl = configuration.ingestionHostUrl();
            String restUrl = baseUrl+"ingestion/register_pipe/";
            String payload = jsonElement.toString();

            //get apikey
            String apiKey = configuration.getApiKey();

            //get apiSecret
            String apiSecret = configuration.getApiSecret();

            //provide response
            JsonObject response = this.handleRestCallForRegisterPipe(restUrl,apiKey,apiSecret, payload);
            response.addProperty("registerPipeStatusCode", response.get("httpResponseCode").getAsString());

            return response;
        }catch(Exception e){
            JsonObject error = new JsonObject();
            error.addProperty("registerPipeError",e.getMessage());
            return error;
        }
    }

    private JsonObject handleRestCallForSendData(String restUrl,String apiKey,String apiSecret,
            String pipeId, String entity, String payload){
        try {
            JsonObject response = new JsonObject();

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("sourceData",payload);
            requestBody.addProperty("pipeId", pipeId);
            requestBody.addProperty("entity", entity);


            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .header("x-api-key", apiKey)
                    .header("x-api-key-secret", apiSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String statusCode = "" + httpResponse.statusCode();

            //TODO: (CR2), for pipeline report service
            JsonElement responseJson = JsonUtil.validateJson(httpResponse.body());
            JsonUtil.printStdOut(responseJson);

            response.addProperty("httpResponseCode", statusCode);


            return response;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private JsonObject handleRestCallForRegisterPipe(String restUrl,String apiKey,String apiSecret, String payload){
        try {
            JsonObject response = new JsonObject();

            JsonObject requestBody = JsonUtil.validateJson(payload).getAsJsonObject();


            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .header("x-api-key", apiKey)
                    .header("x-api-key-secret", apiSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String statusCode = "" + httpResponse.statusCode();

            //TODO: (CR2), for pipeline report service
            JsonElement responseJson = JsonUtil.validateJson(httpResponse.body());
            JsonUtil.printStdOut(responseJson);

            response.addProperty("httpResponseCode", statusCode);
            response.addProperty("registerPipeResult", httpResponse.body());

            return response;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
