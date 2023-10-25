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

public class DataLakeGrapQlQueryClient {
    private static DataLakeGrapQlQueryClient singleton = new DataLakeGrapQlQueryClient();

    private DataLakeGrapQlQueryClient(){

    }

    public static DataLakeGrapQlQueryClient getInstance(){
        //safe-check, cause why not
        if(DataLakeGrapQlQueryClient.singleton == null){
            DataLakeGrapQlQueryClient.singleton = new DataLakeGrapQlQueryClient();
        }
        return DataLakeGrapQlQueryClient.singleton;
    }

    //TODO: finalize_implementation
    public JsonObject sendQuery(String graphqlQuery){
        try {
            String restUrl = "http://localhost:8080/data/lake/query/";

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
            JsonObject response = this.handleRestCall(restUrl,principal,generatedToken, graphqlQuery);
            response.addProperty("queryStatusCode", response.get("httpResponseCode").getAsString());

            return response;
        }catch(Exception e){
            JsonObject error = new JsonObject();
            error.addProperty("queryError",e.getMessage());
            return error;
        }
    }

    //TODO: finalize_implementation
    private JsonObject handleRestCall(String restUrl,String principal,String generatedToken, String graphqlQuery){
        try {
            JsonObject response = new JsonObject();

            //TODO: integrate_entity
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("graphqlQuery",graphqlQuery);


            //TODO: fix authorization
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    //.header("Principal", principal)
                    //.header("Authorization", "Bearer "+generatedToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String statusCode = "" + httpResponse.statusCode();

            response.addProperty("httpResponseCode", statusCode);
            response.addProperty("queryResult", httpResponse.body());

            return response;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
