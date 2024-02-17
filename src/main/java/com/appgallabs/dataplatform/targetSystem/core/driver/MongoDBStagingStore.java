package com.appgallabs.dataplatform.targetSystem.core.driver;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.RecordGenerator;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mongodb.client.*;
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

    @Override
    public String getName() {
        return this.configJson.get("connectionString").getAsString();
    }

    @Override
    public JsonObject getConfiguration() {
        return this.configJson;
    }

    @Override
    public void storeData(Tenant tenant, String pipeId, String entity, List<Record> records) {
        JsonArray dataSet = new JsonArray();
        for(Record record: records){
            JsonObject data = record.getData();
            dataSet.add(data);
        }
        this.storeData(dataSet);
    }

    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        try {
            JsonArray data = this.readData(tenant, pipeId, entity);

            RecordGenerator recordGenerator = new RecordGenerator();
            long offset = 0l;
            List<Record> records = recordGenerator.parsePayload(
                    tenant,
                    pipeId,
                    offset,
                    entity,
                    data.toString()
            );

            return records;
        }catch(Exception e){
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service

            throw new RuntimeException(e);
        }
    }

    //------------------------------------------------------------------------------------
    private void storeData(JsonArray dataSet) {
        try {
            //get the driver configuration
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

    private JsonArray readData(Tenant tenant, String pipeId, String entity){
        try{
            //get the driver configuration
            String database = this.configJson.get("database").getAsString();
            String collection = this.configJson.get("collection").getAsString();

            //setup driver components
            MongoDatabase db = this.mongoClient.getDatabase(database);
            MongoCollection<Document> dbCollection = db.getCollection(collection);

            FindIterable<Document> iterable = dbCollection.find();
            MongoCursor<Document> cursor = iterable.cursor();
            JsonArray data  = new JsonArray();
            while(cursor.hasNext())
            {
                Document document = cursor.next();
                String documentJson = document.toJson();
                JsonObject dataObject = JsonUtil.validateJson(documentJson).getAsJsonObject();
                dataObject.remove("_id");

                data.add(dataObject);
            }

            return data;
        }catch(Exception e){
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service

            throw new RuntimeException(e);
        }
        finally{
            System.out.println(
                    "MONGODB: READ_SUCCESSFULLY");
        }
    }
}
