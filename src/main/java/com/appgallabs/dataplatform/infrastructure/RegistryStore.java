package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.Registry;

import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class RegistryStore implements Serializable {

    public void registerPipe(Tenant tenant, MongoClient mongoClient, JsonObject pipeRegistration){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("registry");

        pipeRegistration.addProperty("tenant", principal);

        collection.insertOne(Document.parse(pipeRegistration.toString()));
    }

    public JsonArray findStagingStores(String principal, MongoClient mongoClient, String pipeId){
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("registry");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("pipeId",pipeId);
        String queryJsonString = queryJson.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject pipeRegistration = JsonUtil.validateJson(documentJson).getAsJsonObject();

            JsonArray result = pipeRegistration.getAsJsonArray("configuration");

            return result;
        }

        return new JsonArray();
    }
}
