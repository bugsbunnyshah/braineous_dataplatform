package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.ehcache.sizeof.SizeOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

@ApplicationScoped
public class PipelineMonitoringService {
    private static Logger logger = LoggerFactory.getLogger(PipelineMonitoringService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public JsonObject preProcess(PipelineServiceType pipelineServiceType,
                                 JsonObject metaData,
                                 SecurityToken securityToken,
                                 String pipeId,
                                 String entity,
                                 String jsonString){
        String principal = securityToken.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        //setup driver components
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = db.getCollection("pipeline_monitoring");

        Queue<String> queue = new LinkedList<>();
        queue.add(jsonString);
        SizeOf sizeOf = SizeOf.newInstance();
        long dataStreamSize = sizeOf.deepSizeOf(queue);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pipelineServiceType", pipelineServiceType.name());
        jsonObject.add("metadata", metaData);
        jsonObject.addProperty("entity", entity);
        jsonObject.addProperty("pipeId", pipeId);
        jsonObject.addProperty("message", jsonString);
        jsonObject.addProperty("sizeInBytes", dataStreamSize);
        jsonObject.addProperty("incoming", true);

        collection.insertOne(Document.parse(jsonObject.toString()));

        return jsonObject;
    }

    public JsonObject postProcess(PipelineServiceType pipelineServiceType,
                                  JsonObject metaData,
                                SecurityToken securityToken,
                                String pipeId,
                                String entity,
                                  String jsonString){
        String principal = securityToken.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        //setup driver components
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = db.getCollection("pipeline_monitoring");

        Queue<String> queue = new LinkedList<>();
        queue.add(jsonString);
        SizeOf sizeOf = SizeOf.newInstance();
        long dataStreamSize = sizeOf.deepSizeOf(queue);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pipelineServiceType", pipelineServiceType.name());
        jsonObject.addProperty("entity", entity);
        jsonObject.add("metadata", metaData);
        jsonObject.addProperty("pipeId", pipeId);
        jsonObject.addProperty("message", jsonString);
        jsonObject.addProperty("sizeInBytes", dataStreamSize);
        jsonObject.addProperty("outgoing", true);

        collection.insertOne(Document.parse(jsonObject.toString()));

        return jsonObject;
    }
}
