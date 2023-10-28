package com.appgallabs.dataplatform.receiver.core.driver;

import com.appgallabs.dataplatform.receiver.framework.StoreDriver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoDBStoreDriver implements StoreDriver {
    private static Logger logger = LoggerFactory.getLogger(MongoDBStoreDriver.class);

    private JsonObject configJson;

    @Override
    public void configure(JsonObject configJson) {
        this.configJson = configJson;
    }

    @Override
    public void storeData(JsonArray dataSet) {
        try {
            //get the driver configuration
            String connectionString = this.configJson.get("connectionString").getAsString();
            String database = this.configJson.get("database").getAsString();
            String collection = this.configJson.get("collection").getAsString();

            //setup driver components
            MongoClient mongoClient = MongoClients.create(connectionString);
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<Document> dbCollection = db.getCollection(collection);

            //bulk insert
            List<WriteModel<Document>> bulkOperations = new ArrayList<>();
            int size = dataSet.size();
            for (int i = 0; i < size; i++) {
                JsonObject dataToBeStored = dataSet.get(i).getAsJsonObject();

                Document document = Document.parse(dataToBeStored.toString());

                InsertOneModel<Document> doc1 = new InsertOneModel<>(document);

                bulkOperations.add(doc1);
            }
            dbCollection.bulkWrite(bulkOperations);

        }catch(Exception e){
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service
        }
    }
}
