package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.PipelineStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SubscriptionService {
    private static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public JsonArray getAllSubscriptions(){
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        //Get all subscriptions
        JsonArray all = pipelineStore.getAllSubscriptions(tenant, mongoClient);

        return all;
    }

    public Subscription getSubscription(String subscriptionId){
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        Subscription subscription = pipelineStore.getSubscription(tenant, mongoClient,
                subscriptionId);

        return subscription;
    }

    public void createSubscription(Subscription subscription){
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        pipelineStore.createSubscription(tenant, mongoClient, subscription);
    }

    public Subscription updateSubscription(Subscription subscription){
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        Subscription updated = pipelineStore.updateSubscription(tenant, mongoClient, subscription);

        return updated;
    }

    public String deleteSubscription(String subscriptionId){
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        String deleted = pipelineStore.deleteSubscription(tenant,mongoClient,subscriptionId);

        return deleted;
    }
}
