package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.pipeline.manager.service.SubscriptionService;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
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
public class SubscriptionTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(SubscriptionTests.class);

    @Inject
    private SubscriptionService subscriptionService;

    @Test
    public void getAllSubscriptions() throws Exception
    {
        int numberOfSubscriptions = 5;

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        for(int i=0; i<numberOfSubscriptions; i++) {
            Subscription subscription = new Subscription(UUID.randomUUID().toString(), group, pushPipe);
            this.subscriptionService.createSubscription(subscription);
        }

        JsonArray all = this.subscriptionService.getAllSubscriptions();
        assertNotNull(all);
        JsonUtil.printStdOut(all);
        assertEquals(numberOfSubscriptions, all.size());
    }

    @Test
    public void getSubscription() throws Exception{
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            this.subscriptionService.createSubscription(subscription);

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
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);

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
    public void updateSubscription() throws Exception {
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            this.subscriptionService.createSubscription(subscription);

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
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());

            //update the subscribers
            String newSubscriber = "3@1.com";
            subscription.getGroup().addSubscriber(new Subscriber(newSubscriber));

            Subscription updated = this.subscriptionService.updateSubscription(subscription);
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
    public void deleteSubscription() throws Exception {
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            this.subscriptionService.createSubscription(subscription);

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
            String deleted = this.subscriptionService.deleteSubscription(subscriptionId);

            Subscription subscription = this.subscriptionService.getSubscription(
                    subscriptionId);

            assertEquals(deleted, subscriptionId);
            assertNull(subscription);
        }
    }
}
