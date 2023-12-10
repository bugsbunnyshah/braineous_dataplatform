package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.PipelineStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.manager.model.*;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PipeService {
    private static Logger logger = LoggerFactory.getLogger(PipeService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private LiveDataFeed liveDataFeed;


    public Pipe moveToDevelopment(Pipe pipe){
        return this.movePipeStage(pipe, PipeStage.DEVELOPMENT);
    }

    public Pipe moveToStaged(Pipe pipe){
        return this.movePipeStage(pipe, PipeStage.STAGED);
    }

    public Pipe moveToDeployed(Pipe pipe){
        return this.movePipeStage(pipe, PipeStage.DEPLOYED);
    }

    private Pipe movePipeStage(Pipe pipe, PipeStage pipeStage){
        String pipeName = pipe.getPipeName();

        //TODO: validate pipeName

        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        pipe = pipelineStore.getPipe(tenant,
                mongoClient,
                pipeName);

        if(pipe == null){
            //create the pipe
            Subscription subscription = new Subscription();
            subscription.setSubscriptionId(UUID.randomUUID().toString());
            subscription = this.subscriptionService.createSubscription(subscription);

            pipe = new Pipe();
            pipe.setPipeId(UUID.randomUUID().toString());
            pipe.setSubscriptionId(subscription.getSubscriptionId());
            pipe.setPipeName(pipeName);
            pipe.setPipeStage(pipeStage);
            pipe.setPipeType(PipeType.PUSH);
            subscription.setPipe(pipe);

            subscription = this.subscriptionService.updateSubscription(subscription);

            return subscription.getPipe();
        }else{
            //update the pipe
            String subscriptionId = pipe.getSubscriptionId();
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);

            pipe.setPipeStage(pipeStage);
            subscription.setPipe(pipe);

            subscription = this.subscriptionService.updateSubscription(subscription);

            return subscription.getPipe();
        }
    }

    //---------------
    public JsonArray allPipes(){
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        JsonArray result = pipelineStore.allPipes(tenant,
                mongoClient);

        return result;
    }

    public JsonArray devPipes(){
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        JsonArray result = pipelineStore.devPipes(tenant,
                mongoClient);

        return result;
    }

    public JsonArray stagedPipes(){
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        JsonArray result = pipelineStore.stagedPipes(tenant,
                mongoClient);

        return result;
    }

    public JsonArray deployedPipes(){
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        JsonArray result = pipelineStore.deployedPipes(tenant,
                mongoClient);

        return result;
    }

    //-----------
    public JsonArray getLiveSnapShot(String clientIp, String snapshotId, String pipeName)
    throws PipeNotFoundException{
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        Pipe pipe = pipelineStore.getPipe(tenant,mongoClient,pipeName);
        if(pipe == null){
            throw new PipeNotFoundException();
        }

        MonitoringSession monitoringSession = new MonitoringSession();
        monitoringSession.setClientIp(clientIp);
        monitoringSession.setSnapShotId(snapshotId);

        MonitoringContext monitoringContext = new MonitoringContext();
        monitoringContext.setSession(monitoringSession);
        monitoringContext.setPipe(pipe);

        List<String> liveFeedSnapshot = this.liveDataFeed.readSnapShot(monitoringContext);

        JsonArray array = JsonUtil.validateJson(liveFeedSnapshot.toString()).getAsJsonArray();

        return array;
    }

    public JsonObject getIngestionStats(String pipeName) throws PipeNotFoundException{
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        Pipe pipe = pipelineStore.getPipe(tenant,mongoClient,pipeName);
        if(pipe == null){
            throw new PipeNotFoundException();
        }

        MonitoringSession monitoringSession = new MonitoringSession();

        MonitoringContext monitoringContext = new MonitoringContext();
        monitoringContext.setSession(monitoringSession);
        monitoringContext.setPipe(pipe);

        JsonObject stats = this.liveDataFeed.prepareIngestionStats(monitoringContext);

        return stats;
    }

    public JsonObject getDeliveryStats(String pipeName) throws PipeNotFoundException{
        Tenant tenant = this.securityTokenContainer.getTenant();
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        Pipe pipe = pipelineStore.getPipe(tenant,mongoClient,pipeName);
        if(pipe == null){
            throw new PipeNotFoundException();
        }

        MonitoringSession monitoringSession = new MonitoringSession();

        MonitoringContext monitoringContext = new MonitoringContext();
        monitoringContext.setSession(monitoringSession);
        monitoringContext.setPipe(pipe);

        JsonObject stats = this.liveDataFeed.prepareDeliveryStats(monitoringContext);

        return stats;
    }
}
