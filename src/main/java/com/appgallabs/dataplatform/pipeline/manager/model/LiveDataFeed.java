package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.manager.service.MonitoringContext;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class LiveDataFeed {

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public LiveDataFeed(){

    }

    public List<String> readSnapShot(MonitoringContext monitoringContext){
        Pipe pipe = monitoringContext.getPipe();
        String pipeId = pipe.getPipeId();

        List<String> snapshot = new ArrayList<>();
        SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();

        String principal = securityToken.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        //setup
        MongoClient mongoClient = systemStore.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = db.getCollection("pipeline_monitoring");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("pipeId", pipeId);

        String queryJsonString = queryJson.toString();
        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            snapshot.add(cour.toString());
        }

        return snapshot;
    }

    public JsonObject prepareIngestionStats(MonitoringContext monitoringContext){
        Pipe pipe = monitoringContext.getPipe();
        String pipeId = pipe.getPipeId();

        List<String> snapshot = new ArrayList<>();
        SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();

        String principal = securityToken.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        //setup
        MongoClient mongoClient = systemStore.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = db.getCollection("pipeline_monitoring");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("pipeId", pipeId);

        String queryJsonString = queryJson.toString();
        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            snapshot.add(cour.toString());
        }

        return null;
    }
}
