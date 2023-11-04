package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.Set;

public class MongoDBDataLakeDriver implements DataLakeDriver, Serializable {
    private static Logger logger = LoggerFactory.getLogger(MongoDBDataLakeDriver.class);

    private String mongodbConnectionString;

    private String mongodbHost;

    private String mongodbPort;

    @Override
    public void configure(String configJson) {

    }

    @Override
    public String name() {
        return "MongoDBDataLakeDriver";
    }

    @Override
    public String storeIngestion(Tenant tenant, Map<String, Object> flatJson) {
        try {
            //TODO cleanup (NOW)
            String debug = flatJson.toString();

            // TODO configure (NOW) get the driver configuration
            String connectionString = "mongodb://localhost:27017";
            String database = "store_1_kyaa";
            String collection = "bhenchod";

            //setup driver components
            MongoClient mongoClient = MongoClients.create(connectionString);
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<Document> dbCollection = db.getCollection(collection);

            Set<Map.Entry<String, Object>> entrySet = flatJson.entrySet();
            Document document = new Document();
            for(Map.Entry<String, Object> entry: entrySet){
                String path = entry.getKey();
                Object value = entry.getValue();
                document.put(path,value);
            }

            dbCollection.insertOne(document);

            /*String datasetLocation = "scenario1/scenario1.json";
            String json = Util.loadResource(datasetLocation);
            JsonArray dataSet = JsonUtil.validateJson(json).getAsJsonArray();

            //bulk insert
            List<WriteModel<Document>> bulkOperations = new ArrayList<>();
            int size = dataSet.size();
            for (int i = 0; i < size; i++) {
                JsonObject dataToBeStored = dataSet.get(i).getAsJsonObject();
                dataToBeStored.addProperty("debug",debug);

                Document document = Document.parse(dataToBeStored.toString());

                InsertOneModel<Document> doc1 = new InsertOneModel<>(document);

                bulkOperations.add(doc1);
            }
            dbCollection.bulkWrite(bulkOperations);*/

            return (String)flatJson.get("objectHash");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonArray readIngestion(Tenant tenant, String dataLakeId) {
        return null;
    }
}
