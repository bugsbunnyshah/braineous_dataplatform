package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoDBDataLakeDriver implements DataLakeDriver, Serializable {
    private static Logger logger = LoggerFactory.getLogger(MongoDBDataLakeDriver.class);

    private String mongodbConnectionString;

    private String mongodbHost;

    private String mongodbPort;

    @Override
    public void configure(JsonObject configJson) {

    }

    @Override
    public String name() {
        return "MongoDBDataLakeDriver";
    }

    @Override
    public String storeIngestion(Tenant tenant, Map<String, Object> flatJson) {
        try {
            //get the driver configuration
            String connectionString = "mongodb://localhost:27017";
            String database = "store_1_kyaa";
            String collection = "bhenchod";

            //setup driver components
            MongoClient mongoClient = MongoClients.create(connectionString);
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<Document> dbCollection = db.getCollection(collection);

            String datasetLocation = "scenario1/scenario1.json";
            String json = Util.loadResource(datasetLocation);
            JsonArray dataSet = JsonUtil.validateJson(json).getAsJsonArray();

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

            return "1";
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonArray readIngestion(Tenant tenant, String dataLakeId) {
        return null;
    }
}
