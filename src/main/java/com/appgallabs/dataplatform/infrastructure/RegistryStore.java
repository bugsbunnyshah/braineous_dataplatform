package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.Registry;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class RegistryStore implements Serializable {

    public void flushToDb(Tenant tenant, MongoClient mongoClient, Registry registry){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("registry");

        String registryJsonString = registry.getRegistry().toString();
        JsonObject registryJson = JsonUtil.validateJson(registryJsonString).getAsJsonObject();

        registryJson.addProperty("tenant", principal);

        collection.insertOne(Document.parse(registryJson.toString()));
    }
}
