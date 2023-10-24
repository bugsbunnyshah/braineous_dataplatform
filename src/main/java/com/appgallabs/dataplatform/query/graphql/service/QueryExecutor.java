package com.appgallabs.dataplatform.query.graphql.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.graphql.engine.QueryParser;
import com.google.gson.JsonArray;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class QueryExecutor {

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private QueryParser queryParser;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public JsonArray executeQuery(String entity, String graphSqlQuery){
        return null;
    }
}
