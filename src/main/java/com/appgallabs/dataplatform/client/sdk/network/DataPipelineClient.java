package com.appgallabs.dataplatform.client.sdk.network;

import com.appgallabs.dataplatform.TempConstants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

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

    //TODO: finalize_implementation (CR1)
    public JsonObject sendData(JsonElement jsonElement){
        try {
            String restUrl = "http://localhost:8080/ingestion/json/";
            String payload = jsonElement.toString();

            //get OAuth Token
            String credentials = IOUtils.resourceToString("oauth/credentials.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject credentialsJson = JsonParser.parseString(credentials).getAsJsonObject();
            String tenant = credentialsJson.get("client_id").getAsString();

            String token = IOUtils.resourceToString("oauth/jwtToken.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject securityTokenJson = JsonParser.parseString(token).getAsJsonObject();
            String generatedToken = securityTokenJson.get("access_token").getAsString();

            //provide response
            JsonObject response = this.handleRestCall(restUrl,tenant,generatedToken, payload);
            response.addProperty("ingestionStatusCode", response.get("httpResponseCode").getAsString());

            return response;
        }catch(Exception e){
            JsonObject error = new JsonObject();
            error.addProperty("ingestionError",e.getMessage());
            return error;
        }
    }

    //TODO: finalize_implementation (CR1)
    private JsonObject handleRestCall(String restUrl,String tenant,String generatedToken, String payload){
        try {
            JsonObject response = new JsonObject();

            String entity = TempConstants.ENTITY;

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("sourceData",payload);
            requestBody.addProperty("entity", entity);


            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .header("tenant", tenant)
                    .header("token", "Bearer "+generatedToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String statusCode = "" + httpResponse.statusCode();

            response.addProperty("httpResponseCode", statusCode);

            return response;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
