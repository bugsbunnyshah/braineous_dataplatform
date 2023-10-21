package com.appgallabs.dataplatform.client.sdk.network;

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

    public JsonObject sendData(JsonElement jsonElement){
        try {
            String restUrl = "http://localhost:8080/graphql/";
            String payload = jsonElement.toString();

            //get OAuth Token
            String credentials = IOUtils.resourceToString("oauth/credentials.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject credentialsJson = JsonParser.parseString(credentials).getAsJsonObject();
            String principal = credentialsJson.get("client_id").getAsString();

            String token = IOUtils.resourceToString("oauth/jwtToken.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject securityTokenJson = JsonParser.parseString(token).getAsJsonObject();
            String generatedToken = securityTokenJson.get("access_token").getAsString();

            //provide response
            JsonObject response = this.handleRestCall(restUrl,principal,generatedToken, payload);
            response.addProperty("ingestionStatusCode", response.get("httpResponseCode").getAsString());

            return response;
        }catch(Exception e){
            JsonObject error = new JsonObject();
            error.addProperty("ingestion_error",e.getMessage());
            return error;
        }
    }

    private JsonObject handleRestCall(String restUrl,String principal,String generatedToken, String payload){
        try {
            JsonObject response = new JsonObject();

            String query = "query documentByLakeId {documentByLakeId(dataLakeId: \"" + "hash" + "\") {data}}";

            String queryJsonString = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().getResourceAsStream("graphql/getDocumentByLakeId.json"),
                    StandardCharsets.UTF_8
            );
            JsonObject queryJsonObject = JsonParser.parseString(queryJsonString).getAsJsonObject();
            queryJsonObject.addProperty("query", query);
            String input = queryJsonObject.toString();


            //TODO: fix authorization
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    //.header("Principal", principal)
                    //.header("Authorization", "Bearer "+generatedToken)
                    .POST(HttpRequest.BodyPublishers.ofString(input))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
            int statusCode = httpResponse.statusCode();

            response.addProperty("httpResponseCode", statusCode);

            return response;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
