package com.appgallabs.dataplatform.infrastructure;

import com.google.gson.JsonObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class TenantStore implements Serializable {
    public void createTenant(Tenant adminTenant, MongoClient mongoClient, Tenant tenant){
        String principal = adminTenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("tenant");

        collection.insertOne(Document.parse(tenant.toJsonForStore().toString()));
    }

    public Tenant getTenant(Tenant adminTenant, MongoClient mongoClient,String apiKey) {
        String principal = adminTenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("tenant");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("apiKey",apiKey);
        String queryJsonString = queryJson.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            Tenant result = Tenant.parse(documentJson);
            return result;
        }

        return null;
    }

    public Tenant getTenant(Tenant adminTenant, MongoClient mongoClient,String name, String email) {
        String principal = adminTenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("tenant");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("name",name);
        queryJson.addProperty("email",email);
        String queryJsonString = queryJson.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            Tenant result = Tenant.parse(documentJson);
            return result;
        }

        return null;
    }
}
