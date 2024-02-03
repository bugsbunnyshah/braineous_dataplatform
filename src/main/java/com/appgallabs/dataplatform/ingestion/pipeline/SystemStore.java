package com.appgallabs.dataplatform.ingestion.pipeline;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.io.Serializable;
import java.text.MessageFormat;

public class SystemStore implements Serializable {
    private String mongodbConnectionString;

    private MongoClient mongoClient;

    public SystemStore(String mongodbConnectionString) {
        this.mongodbConnectionString = mongodbConnectionString;
    }


    public MongoClient getMongoClient() {
        if(this.mongoClient == null) {
            this.mongoClient = MongoClients.create(this.mongodbConnectionString);
        }
        return mongoClient;
    }
}
