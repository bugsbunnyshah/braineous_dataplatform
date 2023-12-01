package com.appgallabs.dataplatform.ingestion.pipeline;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.io.Serializable;
import java.text.MessageFormat;

public class SystemStore implements Serializable {
    private String mongodbConnectionString;

    public SystemStore(String mongodbConnectionString) {
        this.mongodbConnectionString = mongodbConnectionString;
    }

    public String getMongodbConnectionString() {
        return mongodbConnectionString;
    }

    public void setMongodbConnectionString(String mongodbConnectionString) {
        this.mongodbConnectionString = mongodbConnectionString;
    }

    public MongoClient getMongoClient() {
        MongoClient mongoClient = MongoClients.create(this.mongodbConnectionString);
        return mongoClient;
    }
}
