package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.service.DataLakeGraphQlQueryService;
import com.google.gson.JsonArray;

public class DataLakeGraphQlQuery {

    public static JsonArray sendQuery(String graphqlQuery) throws GraphQlQueryException{
        JsonArray result = DataLakeGraphQlQueryService.getInstance().sendQuery(graphqlQuery);
        if(result != null){
            return result;
        }

        //throw exception
        throw new GraphQlQueryException("unknown_query_error");
    }
}
