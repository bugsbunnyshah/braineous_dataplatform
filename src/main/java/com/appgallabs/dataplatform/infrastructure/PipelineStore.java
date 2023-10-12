package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PipelineStore implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(PipelineStore.class);

    public JsonArray getAllSubscriptions(Tenant tenant, MongoClient mongoClient){
        JsonArray all = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject queryJson = new JsonObject();
        String queryJsonString = queryJson.toString();
        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            all.add(cour);
        }

        return all;
    }


    public void createSubscription(Tenant tenant, MongoClient mongoClient, Subscription subscription){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        collection.insertOne(Document.parse(subscription.toJson().toString()));
    }
}
