package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscriber;
import com.appgallabs.dataplatform.pipeline.manager.model.SubscriberGroup;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
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

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        for(int i=0; i<numberOfSubscriptions; i++) {
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

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
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
}
