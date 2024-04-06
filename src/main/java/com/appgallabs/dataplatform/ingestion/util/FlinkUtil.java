package com.appgallabs.dataplatform.ingestion.util;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.enterprise.inject.spi.CDI;

public class FlinkUtil {

    public static void log(String message){
        //setup driver components
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = mongoClient.getDatabase("flink_log");
        MongoCollection<Document> collection = db.getCollection("log");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);

        collection.insertOne(Document.parse(jsonObject.toString()));
    }
}
