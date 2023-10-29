package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.client.sdk.api.GraphQlQueryException;
import com.appgallabs.dataplatform.client.sdk.network.DataLakeGrapQlQueryClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataLakeGraphQlQueryService {
    private static DataLakeGraphQlQueryService singleton = new DataLakeGraphQlQueryService();

    private DataLakeGrapQlQueryClient client;

    private DataLakeGraphQlQueryService(){
        this.client = DataLakeGrapQlQueryClient.getInstance();
    }

    public static DataLakeGraphQlQueryService getInstance(){
        //safe-check, cause why not
        if(DataLakeGraphQlQueryService.singleton == null){
            DataLakeGraphQlQueryService.singleton = new DataLakeGraphQlQueryService();
        }
        return DataLakeGraphQlQueryService.singleton;
    }

    public JsonArray sendQuery(String graphqlQuery) throws GraphQlQueryException{
        //send query
        JsonObject response = this.client.sendQuery(graphqlQuery);

        //process response
        String queryStatusMessage = null;
        if(response.has("queryError")){
            queryStatusMessage = response.get("queryError").getAsString();
        }else{
            queryStatusMessage = response.get("queryStatusCode").getAsString();
        }
        response.addProperty("status",queryStatusMessage);

        JsonArray result = new JsonArray();
        if(response.has("queryResult") && queryStatusMessage.equals("200")){
            String queryResult = response.get("queryResult").getAsString();
            result = JsonParser.parseString(queryResult).getAsJsonArray();
            return result;
        }

        throw new GraphQlQueryException(response.toString());
    }
}
