package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PipelineStore implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(PipelineStore.class);

    public List<Subscription> getAllSubscriptions(Tenant tenant, MongoClient mongoClient){
        List<Subscription> all = new ArrayList<>();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        return all;
    }


    public void createSubscription(Tenant tenant, MongoClient mongoClient, Subscription subscription){
        logger.info("*******************");
        logger.info(tenant.toString());
        logger.info("*******************");
    }
}
