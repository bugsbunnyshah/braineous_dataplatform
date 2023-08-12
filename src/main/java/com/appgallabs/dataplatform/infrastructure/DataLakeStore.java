package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Singleton
public class DataLakeStore implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(DataLakeStore.class);

    public JsonArray readByEntity(Tenant tenant, MongoClient mongoClient, String entity){
        JsonArray entities = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String queryJson = "{\"entity\":\""+entity+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            entities.add(cour);
        }

        return entities;
    }

    public boolean entityExists(Tenant tenant, MongoClient mongoClient, JsonObject json){
        boolean exists = false;

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String objectHash = json.get("objectHash").getAsString();
        String queryJson = "{\"objectHash\":\""+objectHash+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        exists = iterable.iterator().hasNext();

        return exists;
    }

    public JsonObject readEntity(Tenant tenant, MongoClient mongoClient,String objectHash) {
        JsonObject result = null;

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String queryJson = "{\"objectHash\":\"" + objectHash + "\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if (cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            result = JsonParser.parseString(documentJson).getAsJsonObject();
        }

        return result;
    }
}
