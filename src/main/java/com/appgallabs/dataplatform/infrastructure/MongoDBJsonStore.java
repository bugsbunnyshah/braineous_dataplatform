package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.configuration.AIPlatformConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

@Singleton
public class MongoDBJsonStore implements Serializable
{
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStore.class);

    @Inject
    private AIPlatformConfig aiPlatformConfig;

    @Inject
    private DataHistoryStore dataHistoryStore;

    private MongoClient mongoClient;
    private Map<String,MongoDatabase> databaseMap;

    public MongoDBJsonStore()
    {
        this.databaseMap = new HashMap<>();
    }

    @PostConstruct
    public void start()
    {
        try {
            JsonObject config = this.aiPlatformConfig.getConfiguration();

            //mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
            StringBuilder connectStringBuilder = new StringBuilder();
            connectStringBuilder.append("mongodb://");

            String mongodbHost = config.get("mongodbHost").getAsString();
            long mongodbPort = config.get("mongodbPort").getAsLong();
            if (config.has("mongodbUser") && config.has("mongodbPassword")) {
                connectStringBuilder.append(config.get("mongodbUser").getAsString()
                        + ":" + config.get("mongodbPassword").getAsString() + "@");
            }
            connectStringBuilder.append(mongodbHost + ":" + mongodbPort);

            String connectionString = connectStringBuilder.toString();
            this.mongoClient = MongoClients.create(connectionString);
        }
        catch(Exception e)
        {
            this.mongoClient = null;
        }
    }

    @PreDestroy
    public void stop()
    {
        this.mongoClient.close();
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    //Data Ingestion related operations-----------------------------------------------------
    public void storeIngestion(Tenant tenant,JsonObject jsonObject)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        collection.insertOne(Document.parse(jsonObject.toString()));
    }

    public JsonArray getIngestion(Tenant tenant,String dataLakeId)
    {
        JsonArray ingestion = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String queryJson = "{\"braineous_datalakeid\":\""+dataLakeId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject storedJson = JsonParser.parseString(documentJson).getAsJsonObject();
            ingestion.add(storedJson);
        }
        return ingestion;
    }

    public JsonArray getIngestedDataSet(Tenant tenant)
    {
        JsonArray ingestedDataSet = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject ingestion = JsonParser.parseString(documentJson).getAsJsonObject();
            JsonObject actual = new JsonObject();
            actual.addProperty("ingestionId", ingestion.get("ingestionId").getAsString());
            actual.addProperty("data", ingestion.get("data").getAsString());
            ingestedDataSet.add(actual);
        }
        return ingestedDataSet;
    }

    public JsonArray getIngestedDataSetsMetaData(Tenant tenant)
    {
        JsonArray ingestedDataSet = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String queryJson = "{\"tenant\":\""+tenant.getPrincipal()+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject ingestion = JsonParser.parseString(documentJson).getAsJsonObject();
            JsonObject actual = new JsonObject();
            actual.addProperty("dataLakeId", ingestion.get("braineous_datalakeid").getAsString());
            actual.addProperty("chainId", ingestion.get("chainId").getAsString());
            ingestedDataSet.add(actual);
        }
        return ingestedDataSet;
    }
    //Data History related operations-----------------------------------------------------
    public String startDiffChain(Tenant tenant,JsonObject payload)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffchain");

        String datalakeid;
        String chainId;
        if(payload.has("braineous_datalakeid")) {
            datalakeid = payload.get("braineous_datalakeid").getAsString();
        }
        else {
            datalakeid = UUID.randomUUID().toString();
        }
        chainId = "/" + principal + "/" + datalakeid;


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("payload", payload);


        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);

        return chainId;
    }

    public void addToDiffChain(Tenant tenant,String chainId, JsonObject payload)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffchain");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("payload", payload);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public void addToDiffChain(Tenant tenant,String requestChainId, String chainId, JsonObject payload)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffchain");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.addProperty("requestChainId", requestChainId);
        jsonObject.add("payload", payload);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public JsonObject getLastPayload(Tenant tenant,String chainId)
    {
        JsonObject lastPayload = new JsonObject();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("diffchain");

        String queryJson = "{\"chainId\":\""+chainId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            if(!cursor.hasNext())
            {
                String documentJson = document.toJson();
                lastPayload = JsonParser.parseString(documentJson).getAsJsonObject();
            }
        }

        lastPayload = lastPayload.getAsJsonObject("payload");
        return lastPayload;
    }

    public List<JsonObject> readDiffChain(Tenant tenant,String chainId)
    {
        List<JsonObject> chain = new LinkedList<>();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffchain");

        String queryJson = "{\"chainId\":\""+chainId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject objectDiff = JsonParser.parseString(documentJson).getAsJsonObject();
            chain.add(objectDiff);
        }

        return chain;
    }

    public void addToDiff(Tenant tenant,String chainId, JsonObject objectDiff)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("diff");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("objectDiff", objectDiff);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public void addToDiff(Tenant tenant,String requestChainId, String chainId, JsonObject objectDiff)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("diff");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.addProperty("requestChainId", requestChainId);
        jsonObject.add("objectDiff", objectDiff);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public List<JsonObject> readDiffs(Tenant tenant,String chainId)
    {
        List<JsonObject> diffs = new LinkedList<>();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diff");

        String queryJson = "{\"chainId\":\""+chainId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject objectDiff = JsonParser.parseString(documentJson).getAsJsonObject();
            diffs.add(objectDiff);
        }

        return diffs;
    }
    //-----------------------------------------------------------------------------
    public String storeModel(Tenant tenant,JsonObject modelPackage)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");
        String modelId = UUID.randomUUID().toString();
        modelPackage.addProperty("modelId", modelId);
        Document doc = Document.parse(modelPackage.toString());
        collection.insertOne(doc);

        return modelId;
    }

    public String getModel(Tenant tenant,String modelId)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");

        String queryJson = "{\"modelId\":\""+modelId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            return JsonParser.parseString(documentJson).getAsJsonObject().get("model").getAsString();
        }
        return null;
    }

    public JsonObject getModelPackage(Tenant tenant,String modelId)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");

        String queryJson = "{\"modelId\":\""+modelId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            return JsonParser.parseString(documentJson).getAsJsonObject();
        }
        return null;
    }

    public void deployModel(Tenant tenant,String modelId)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("aimodels");

        JsonObject currentModel = this.getModelPackage(tenant,modelId);
        Bson bson = Document.parse(currentModel.toString());
        collection.deleteOne(bson);

        currentModel.remove("_id");
        currentModel.addProperty("live", true);
        this.storeLiveModel(tenant,currentModel);
    }

    public void undeployModel(Tenant tenant,String modelId)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("aimodels");

        JsonObject currentModel = this.getModelPackage(tenant,modelId);
        Bson bson = Document.parse(currentModel.toString());
        collection.deleteOne(bson);

        currentModel.remove("_id");
        currentModel.addProperty("live", false);
        this.storeLiveModel(tenant,currentModel);
    }

    private void storeLiveModel(Tenant tenant,JsonObject modelPackage)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");
        Document doc = Document.parse(modelPackage.toString());
        collection.insertOne(doc);
    }
    //DataLake related operations----------------------------------------------------------------
    public String storeTrainingDataSet(Tenant tenant,JsonObject dataSetJson)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String oid = UUID.randomUUID().toString();
        dataSetJson.addProperty("dataSetId", oid);
        dataSetJson.addProperty("dataSetType", "training");
        Document doc = Document.parse(dataSetJson.toString());
        collection.insertOne(doc);

        return oid;
    }

    public String storeTrainingDataSetInLake(Tenant tenant,JsonObject dataSetJson)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String oid = UUID.randomUUID().toString();
        dataSetJson.addProperty("dataSetId", oid);
        dataSetJson.addProperty("dataSetType", "training");
        Document doc = Document.parse(dataSetJson.toString());
        collection.insertOne(doc);

        return oid;
    }

    public String storeEvalDataSet(Tenant tenant,JsonObject dataSetJson)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String oid = UUID.randomUUID().toString();
        dataSetJson.addProperty("dataSetId", oid);
        dataSetJson.addProperty("dataSetType", "evaluation");
        Document doc = Document.parse(dataSetJson.toString());
        collection.insertOne(doc);

        return oid;
    }

    public JsonObject readDataSet(Tenant tenant,String dataSetId)
    {
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String queryJson = "{\"dataSetId\":\""+dataSetId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            return cour;
        }

        return null;
    }

    public JsonObject rollOverToTraningDataSets(Tenant tenant,String modelId)
    {
        JsonObject rolledOverDataSetIds = new JsonObject();

        JsonArray dataSetIds = new JsonArray();
        String dataSettype = "training";
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String queryJson = "{\"modelId\":\""+modelId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject dataSetJson = JsonParser.parseString(documentJson).getAsJsonObject();
            dataSetJson.remove("_id");
            dataSetJson.addProperty("dataSetType", "training");
            collection.insertOne(Document.parse(dataSetJson.toString()));
            dataSetIds.add(dataSetJson.get("dataSetId").getAsString());
        }

        rolledOverDataSetIds.add("rolledOverDataSetIds", dataSetIds);
        return rolledOverDataSetIds;
    }
    //---DataHistory----------------------------
    public void storeHistoryObject(Tenant tenant, JsonObject jsonObject){
        this.dataHistoryStore.storeHistoryObject(tenant, this.mongoClient,jsonObject);
    }

    public JsonArray readHistory(Tenant tenant, OffsetDateTime endTime){
        return this.dataHistoryStore.readHistory(tenant, this.mongoClient,endTime);
    }
}
