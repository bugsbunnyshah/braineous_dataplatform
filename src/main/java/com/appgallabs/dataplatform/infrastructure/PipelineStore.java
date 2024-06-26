package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.PipeStage;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PipelineStore implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(PipelineStore.class);

    public void createSubscription(Tenant tenant, MongoClient mongoClient, Subscription subscription){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        collection.insertOne(Document.parse(subscription.toJson().toString()));
    }

    public JsonArray getAllSubscriptions(Tenant tenant, MongoClient mongoClient){
        JsonArray all = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject queryJson = new JsonObject();
        String queryJsonString = queryJson.toString();
        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            all.add(cour);
        }

        return all;
    }

    public Subscription getSubscription(Tenant tenant, MongoClient mongoClient,String subscriptionId){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("subscriptionId",subscriptionId);
        String queryJsonString = queryJson.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            Subscription result = Subscription.parse(documentJson);
            return result;
        }

        return null;
    }

    public Subscription updateSubscription(Tenant tenant, MongoClient mongoClient,Subscription subscription){
        String subscriptionId = subscription.getSubscriptionId();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("subscriptionId",subscriptionId);
        String queryJsonString = queryJson.toString();
        Bson bson = Document.parse(queryJsonString);

        Document newDocument = Document.parse(subscription.toJson().toString());
        collection.findOneAndReplace(bson, newDocument);

        Subscription updatedSubscription = this.getSubscription(tenant,mongoClient,subscriptionId);

        return updatedSubscription;
    }

    public String deleteSubscription(Tenant tenant, MongoClient mongoClient,String subscriptionId){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject queryJson = new JsonObject();
        queryJson.addProperty("subscriptionId",subscriptionId);
        String queryJsonString = queryJson.toString();
        Bson bson = Document.parse(queryJsonString);

        collection.deleteOne(bson);

        return subscriptionId;
    }

    //-------------------------------------------------------
    public void updatePipe(Tenant tenant, MongoClient mongoClient, Pipe pipe) {
        String subscriptionId = pipe.getSubscriptionId();
        Subscription subscription = this.getSubscription(tenant,mongoClient,subscriptionId);

        subscription.setPipe(pipe);

        this.updateSubscription(tenant,mongoClient,subscription);
    }

    public Pipe getPipe(Tenant tenant, MongoClient mongoClient, String pipeName){

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject criteria = new JsonObject();
        criteria.addProperty("pipe.pipeName", pipeName);
        String queryJsonString = criteria.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject member = JsonUtil.validateJson(documentJson).getAsJsonObject();
            member.remove("_id");

            Subscription subscription = Subscription.parse(member.toString());

            return subscription.getPipe();
        }

        return null;
    }

    public JsonArray devPipes(Tenant tenant, MongoClient mongoClient){
        JsonArray result = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject criteria = new JsonObject();
        criteria.addProperty("pipe.pipeStage", String.valueOf(PipeStage.DEVELOPMENT));
        String queryJsonString = criteria.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject member = JsonUtil.validateJson(documentJson).getAsJsonObject();
            member.remove("_id");

            result.add(member);
        }

        return result;
    }

    public JsonArray stagedPipes(Tenant tenant, MongoClient mongoClient){
        JsonArray result = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject criteria = new JsonObject();
        criteria.addProperty("pipe.pipeStage", String.valueOf(PipeStage.STAGED));
        String queryJsonString = criteria.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject member = JsonUtil.validateJson(documentJson).getAsJsonObject();
            member.remove("_id");

            result.add(member);
        }

        return result;
    }

    public JsonArray deployedPipes(Tenant tenant, MongoClient mongoClient){
        JsonArray result = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject criteria = new JsonObject();
        criteria.addProperty("pipe.pipeStage", String.valueOf(PipeStage.DEPLOYED));
        String queryJsonString = criteria.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject member = JsonUtil.validateJson(documentJson).getAsJsonObject();
            member.remove("_id");

            result.add(member);
        }

        return result;
    }

    public JsonArray allPipes(Tenant tenant, MongoClient mongoClient){
        JsonArray result = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("subscription");

        JsonObject criteria = new JsonObject();
        String queryJsonString = criteria.toString();

        Bson bson = Document.parse(queryJsonString);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject member = JsonUtil.validateJson(documentJson).getAsJsonObject();
            member.remove("_id");

            result.add(member);
        }

        return result;
    }
}
