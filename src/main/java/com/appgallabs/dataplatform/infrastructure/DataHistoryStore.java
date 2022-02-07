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
import java.time.OffsetDateTime;

@Singleton
public class DataHistoryStore {
    private static Logger logger = LoggerFactory.getLogger(DataHistoryStore.class);

    public void storeHistoryObject(Tenant tenant, MongoClient mongoClient, JsonObject json){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        if(this.exists(tenant,mongoClient,json)){
            return;
        }

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("datahistory");
        collection.insertOne(Document.parse(json.toString()));
    }

    private boolean exists(Tenant tenant, MongoClient mongoClient, JsonObject json){
        JsonUtil.printStdOut(json);
        boolean exists = false;

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datahistory");

        String oid = json.get("oid").getAsString();
        String queryJson = "{\"oid\":\""+oid+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        exists = iterable.iterator().hasNext();

        return exists;
    }

    public JsonArray readHistory(Tenant tenant, MongoClient mongoClient, OffsetDateTime endTime){
        JsonArray history = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datahistory");

        String queryJson = "{\"timestamp\":{ $lte: "+endTime.toEpochSecond()+"}}";
        System.out.println("********************");
        System.out.println(queryJson);
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            history.add(cour);
        }

        return history;
    }
}
