package com.appgallabs.dataplatform.infrastructure;

import com.google.gson.JsonObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class TenantStore implements Serializable {

    public boolean doesTenantExist(MongoClient mongoClient, String name, String email){
        String databaseName = "braineous_system";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("registered_tenants");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("name",name);
        queryJson.addProperty("email", email);
        String queryJsonString = queryJson.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            return true;
        }

        return false;
    }

    public void createTenant(MongoClient mongoClient, Tenant tenant){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("tenant");
        collection.insertOne(Document.parse(tenant.toJsonForStore().toString()));

        databaseName = "braineous_system";
        database = mongoClient.getDatabase(databaseName);
        collection = database.getCollection("registered_tenants");
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

    public Tenant getTenantByEmail(MongoClient mongoClient,String email) {
        String databaseName = "braineous_system";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("registered_tenants");

        JsonObject queryJson = new JsonObject();
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
