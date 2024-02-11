package com.appgallabs.dataplatform.targetSystem.core.driver;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;

import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
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

public class MongoDBStagingStore implements StagingStore {
    private static Logger logger = LoggerFactory.getLogger(MongoDBStagingStore.class);

    private JsonObject configJson;
    private MongoClient mongoClient;



    @Override
    public void configure(JsonObject configJson) {
        this.configJson = configJson;

        //get the driver configuration
        String connectionString = this.configJson.get("connectionString").getAsString();

        //setup driver components
        this.mongoClient = MongoClients.create(connectionString);
    }

    //@Override
    public void storeData(JsonArray dataSet) {
        try {
            //get the driver configuration
            String connectionString = this.configJson.get("connectionString").getAsString();
            String database = this.configJson.get("database").getAsString();
            String collection = this.configJson.get("collection").getAsString();

            //setup driver components
            MongoDatabase db = this.mongoClient.getDatabase(database);
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
        finally{
            System.out.println(
                    "MONGODB: STORED_SUCCESSFULLY");
        }
    }

    @Override
    public String getName() {
        return this.configJson.get("connectionString").getAsString();
    }

    @Override
    public JsonObject getConfiguration() {
        return this.configJson;
    }

    @Override
    public void storeData(Tenant tenant, String pipeId, String entity, List<Record> dataSet) {

    }

    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        return null;
    }
}
