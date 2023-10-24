package com.appgallabs.dataplatform.query.graphql.service;

import com.appgallabs.dataplatform.infrastructure.DataLakeStore;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.graphql.engine.QueryParser;
import com.appgallabs.dataplatform.query.graphql.engine.QueryParserResult;

import com.google.gson.JsonArray;
import com.mongodb.client.MongoClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class QueryExecutor {

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private QueryParser queryParser;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public JsonArray executeQueryNoCriteria(String entity, String graphSqlQuery){
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        DataLakeStore dataLakeStore = this.mongoDBJsonStore.getDataLakeStore();

        //parse the graphSqlQuery
        QueryParserResult queryParserResult = this.queryParser.parseQuery(entity,graphSqlQuery);

        //execute query on MongoDB datalake
        List<String> projectionFields = queryParserResult.getProjectionFields();

        JsonArray result = dataLakeStore.readByEntity(tenant,mongoClient,
                entity, projectionFields);

        return result;
    }

    public JsonArray executeQueryByANDCriteria(String entity, String graphSqlQuery){
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        DataLakeStore dataLakeStore = this.mongoDBJsonStore.getDataLakeStore();

        //parse the graphSqlQuery
        QueryParserResult queryParserResult = this.queryParser.parseQuery(entity,graphSqlQuery);

        //execute query on MongoDB datalake
        List<String> projectionFields = queryParserResult.getProjectionFields();
        Map<String,String> criteria = queryParserResult.getCriteria();

        JsonArray result = dataLakeStore.readByEntityFilterByAND(tenant,mongoClient,
                entity, projectionFields, criteria);

        return result;
    }

    public JsonArray executeQueryByORCriteria(String entity, String graphSqlQuery){
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        DataLakeStore dataLakeStore = this.mongoDBJsonStore.getDataLakeStore();

        //parse the graphSqlQuery
        QueryParserResult queryParserResult = this.queryParser.parseQuery(entity,graphSqlQuery);

        //execute query on MongoDB datalake
        List<String> projectionFields = queryParserResult.getProjectionFields();
        Map<String,String> criteria = queryParserResult.getCriteria();

        JsonArray result = dataLakeStore.readByEntityFilterByOR(tenant,mongoClient,
                entity, projectionFields, criteria);

        return result;
    }
}
