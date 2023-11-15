package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.*;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.mongodb.client.MongoClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PipelineStoreTests extends BaseTest {
    private Logger logger = LoggerFactory.getLogger(PipelineStoreTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;


    @Test
    public void getAllSubscriptions() throws Exception{
        int numberOfSubscriptions = 5;
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));


        for(int i=0; i<numberOfSubscriptions; i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            Subscription subscription = new Subscription(UUID.randomUUID().toString(), group, pushPipe);
            pipelineStore.createSubscription(tenant, mongoClient, subscription);
        }

        //Get all subscriptions
        JsonArray all = pipelineStore.getAllSubscriptions(tenant, mongoClient);

        assertNotNull(all);
        JsonUtil.printStdOut(all);
        assertEquals(numberOfSubscriptions, all.size());
    }

    @Test
    public void getSubscription() throws Exception{
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            pipelineStore.createSubscription(tenant, mongoClient, subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    subscriptionId);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            logger.info("****SUBSCRIPTION_ID_HASH_READ***");
            logger.info("ID: " +subscriptionId);
            logger.info("HASH: " + hash);
            logger.info("**********************");

            assertNotNull(subscription);
            assertTrue(subscriptionHashes.contains(hash));
        }
    }

    @Test
    public void updateSubscription() throws Exception{
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));


        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            pipelineStore.createSubscription(tenant, mongoClient, subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("HASH: " + hash);
            JsonUtil.printStdOut(subscription.toJson());
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());

            //update the subscribers
            String newSubscriber = "3@1.com";
            subscription.getGroup().addSubscriber(new Subscriber(newSubscriber));

            Subscription updated = pipelineStore.updateSubscription(tenant, mongoClient, subscription);
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
        }
    }

    @Test
    public void deleteSubscription() throws Exception{
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));


        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            pipelineStore.createSubscription(tenant, mongoClient, subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            String deleted = pipelineStore.deleteSubscription(tenant,mongoClient,subscriptionId);

            Subscription subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    subscriptionId);

            assertEquals(deleted, subscriptionId);
            assertNull(subscription);
        }
    }

    @Test
    public void updatePipeStage() throws Exception{
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));



        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        List<Pipe> pipes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            String subscriptionId = UUID.randomUUID().toString();
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),UUID.randomUUID().toString());
            pipes.add(pushPipe);

            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            pipelineStore.createSubscription(tenant, mongoClient, subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");

        for(Pipe pipe: pipes){
            Subscription subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    pipe.getSubscriptionId());
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            logger.info("****PIPE_HASH_PRE_UPDATE***");
            logger.info("ID: " +subscription.getSubscriptionId());
            logger.info("HASH: " + hash);
            logger.info("**************************");
            assertTrue(subscriptionHashes.contains(hash));

            pipe.setPipeStage(PipeStage.DEPLOYED);
            pipelineStore.updatePipe(tenant,mongoClient,pipe);

            subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    pipe.getSubscriptionId());

            String newHash = JsonUtil.getJsonHash(subscription.toJson());
            JsonUtil.printStdOut(subscription.toJson());
            logger.info("****PIPE_HASH_POST_UPDATE***");
            logger.info("ID: " +subscription.getSubscriptionId());
            logger.info("HASH: " + newHash);
            logger.info("**************************");
            assertNotEquals(hash, newHash);
        }
    }

    @Test
    public void updatePipeDataCleansingFunctions() throws Exception{
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));



        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        List<Pipe> pipes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            String subscriptionId = UUID.randomUUID().toString();
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),UUID.randomUUID().toString());
            pipes.add(pushPipe);

            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            pipelineStore.createSubscription(tenant, mongoClient, subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");

        for(Pipe pipe: pipes){
            Subscription subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    pipe.getSubscriptionId());
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            logger.info("****PIPE_HASH_PRE_UPDATE***");
            logger.info("ID: " +subscription.getSubscriptionId());
            logger.info("HASH: " + hash);
            logger.info("**************************");
            assertTrue(subscriptionHashes.contains(hash));

            DataCleanerFunction dataCleanerFunction = new DataCleanerFunction("HELLO_WORLD");
            pipe.addCleanerFunction(dataCleanerFunction);
            pipelineStore.updatePipe(tenant,mongoClient,pipe);

            subscription = pipelineStore.getSubscription(tenant, mongoClient,
                    pipe.getSubscriptionId());

            String newHash = JsonUtil.getJsonHash(subscription.toJson());
            JsonUtil.printStdOut(subscription.toJson());
            logger.info("****PIPE_HASH_POST_UPDATE***");
            logger.info("ID: " +subscription.getSubscriptionId());
            logger.info("HASH: " + newHash);
            logger.info("**************************");
            assertNotEquals(hash, newHash);
        }
    }
}
