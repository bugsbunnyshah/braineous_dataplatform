package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.*;
import com.appgallabs.dataplatform.util.JsonUtil;
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
public class DataCleansingServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(DataCleansingServiceTests.class);

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private PipeService pipeService;

    @Inject
    private DataCleansingService dataCleansingService;

    @Test
    public void addCleanerFunction() throws Exception
    {
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);

            this.subscriptionService.createSubscription(subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("PipeStageBefore: " +subscription.getPipe().getPipeStage());
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            Pipe old = subscription.getPipe();

            //update the pipe
            DataCleanerFunction dataCleanerFunction = new DataCleanerFunction("HELLO_WORLD");
            Pipe updated = this.dataCleansingService.addCleanerFunction(old,dataCleanerFunction);
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(old.toJson());
            logger.info("*****************************************************************");
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertTrue(old.getCleanerFunctions().size() == 0);
            assertTrue(updated.getCleanerFunctions().size() == 1);
        }
    }

    @Test
    public void removeCleanerFunction() throws Exception{
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe"+i);

            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            DataCleanerFunction dataCleanerFunction = new DataCleanerFunction("HELLO_WORLD");
            pushPipe.addCleanerFunction(dataCleanerFunction);
            this.subscriptionService.createSubscription(subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("PipeStageBefore: " +subscription.getPipe().getPipeStage());
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            Pipe old = subscription.getPipe();

            //update the pipe
            DataCleanerFunction dataCleanerFunction = new DataCleanerFunction("HELLO_WORLD");
            Pipe updated = this.dataCleansingService.removeCleanerFunction(old,dataCleanerFunction);
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(old.toJson());
            logger.info("*****************************************************************");
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertTrue(old.getCleanerFunctions().size() == 1);
            assertTrue(updated.getCleanerFunctions().size() == 0);
        }
    }

    @Test
    public void removeAllCleanerFunctions() throws Exception {
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe"+i);

            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);
            DataCleanerFunction dataCleanerFunction1 = new DataCleanerFunction("HELLO_WORLD_1");
            DataCleanerFunction dataCleanerFunction2 = new DataCleanerFunction("HELLO_WORLD_2");
            pushPipe.addCleanerFunction(dataCleanerFunction1);
            pushPipe.addCleanerFunction(dataCleanerFunction2);
            this.subscriptionService.createSubscription(subscription);

            String hash = JsonUtil.getJsonHash(subscription.toJson());
            subscriptionIds.add(subscriptionId);
            subscriptionHashes.add(hash);

            logger.info("****SUBSCRIPTION_HASH_CREATE***");
            logger.info("ID: " +subscriptionId);
            logger.info("PipeStageBefore: " +subscription.getPipe().getPipeStage());
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            Pipe old = subscription.getPipe();

            //update the pipe
            Pipe updated = this.dataCleansingService.removeAllCleanerFunctions(old);
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(old.toJson());
            logger.info("*****************************************************************");
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertTrue(old.getCleanerFunctions().size() == 2);
            assertTrue(updated.getCleanerFunctions().size() == 0);
        }
    }
}
