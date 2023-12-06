package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.PipelineStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.manager.model.LiveDataFeed;
import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.PipeStage;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
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


    public Pipe moveToDevelopment(Pipe pipe){
        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(UUID.randomUUID().toString());
        subscription = this.subscriptionService.createSubscription(subscription);

        pipe.setPipeId(UUID.randomUUID().toString());
        subscription.setPipe(pipe);

        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public Pipe moveToStaged(Pipe pipe){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
        subscription.getPipe().setPipeStage(PipeStage.STAGED);
        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public Pipe moveToDeployed(Pipe pipe){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
        subscription.getPipe().setPipeStage(PipeStage.DEPLOYED);
        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public List<String> getLiveSnapShot(String clientIp, String snapshotId, Pipe pipe){
        MonitoringSession monitoringSession = new MonitoringSession();
        monitoringSession.setClientIp(clientIp);
        monitoringSession.setSnapShotId(snapshotId);

        MonitoringContext monitoringContext = new MonitoringContext();
        monitoringContext.setSession(monitoringSession);
        monitoringContext.setPipe(pipe);

        LiveDataFeed liveDataFeed = new LiveDataFeed();
        List<String> liveFeedSnapshot = liveDataFeed.readSnapShot(monitoringContext);

        return liveFeedSnapshot;
    }

    //---------------
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
}
