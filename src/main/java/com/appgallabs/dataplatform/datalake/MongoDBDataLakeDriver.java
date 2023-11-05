package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.Debug;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class MongoDBDataLakeDriver implements DataLakeDriver, Serializable {
    private static Logger logger = LoggerFactory.getLogger(MongoDBDataLakeDriver.class);

    private String connectionString;
    private String collection;

    @Override
    public void configure(String configJson) {
        JsonObject datalakeConfig = JsonUtil.validateJson(configJson).getAsJsonObject()
                .get("datalake").getAsJsonObject()
                .get("configuration").getAsJsonObject();

        this.connectionString = datalakeConfig.get("connectionString").getAsString();
        this.collection = datalakeConfig.get("collection").getAsString();
    }

    @Override
    public String name() {
        return "MongoDBDataLakeDriver";
    }

    @Override
    public void storeIngestion(Tenant tenant, String jsonObjectString) {
        try {
            String principal = tenant.getPrincipal();
            String databaseName = principal + "_" + "aiplatform";

            //setup driver components
            MongoClient mongoClient = MongoClients.create(connectionString);
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> dbCollection = db.getCollection(collection);

            //store
            Document document = Document.parse(jsonObjectString);

            dbCollection.insertOne(document);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
