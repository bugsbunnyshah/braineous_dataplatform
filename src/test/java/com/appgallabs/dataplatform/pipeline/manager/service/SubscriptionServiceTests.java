package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscriber;
import com.appgallabs.dataplatform.pipeline.manager.model.SubscriberGroup;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;

import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.ApiUtil;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
public class SubscriptionServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(SubscriptionServiceTests.class);

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void getAllSubscriptions() throws Exception
    {
        int numberOfSubscriptions = 5;

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        for(int i=0; i<numberOfSubscriptions; i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            Subscription subscription = new Subscription(UUID.randomUUID().toString(), group, pushPipe);
            this.subscriptionService.createSubscription(subscription);
        }

        JsonArray all = this.subscriptionService.getAllSubscriptions();
        assertNotNull(all);
        JsonUtil.printStdOut(all);
        assertEquals(numberOfSubscriptions, all.size());
    }

    @Test
    public void getAllSubscriptionsEndpoint() throws Exception
    {
        int numberOfSubscriptions = 5;

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        for(int i=0; i<numberOfSubscriptions; i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            Subscription subscription = new Subscription(UUID.randomUUID().toString(), group, pushPipe);
            this.subscriptionService.createSubscription(subscription);
        }

        String endpoint = "/subscription_manager/subscriptions_all/";
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonArray all = ApiUtil.apiGetRequest(endpoint, securityToken).getAsJsonArray();
        assertNotNull(all);
        JsonUtil.printStdOut(all);
        assertEquals(numberOfSubscriptions, all.size());
    }

    @Test
    public void getSubscription() throws Exception{
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
    public void getSubscriptionEndpoint() throws Exception{
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
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            String endpoint = "/subscription_manager/get_subscription/"+subscriptionId+"/";
            SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
            JsonObject responseJson = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonObject();
            Subscription subscription = Subscription.parse(responseJson.toString());

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
    public void updateSubscriptionEndpoint() throws Exception {
        //create subscription
        String endpoint = "/subscription_manager/create_subscription/";
        JsonObject payload = new JsonObject();
        String subscriptionId = UUID.randomUUID().toString();
        payload.addProperty("subscriptionId", subscriptionId);
        String payloadHash = JsonUtil.getJsonHash(payload);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiPostRequest(endpoint,payload.toString(),securityToken).getAsJsonObject();
        JsonObject subscriptionJson = responseJson.getAsJsonObject("subscription");
        Subscription subscription = Subscription.parse(subscriptionJson.toString());

        String subscriptionHash = JsonUtil.getJsonHash(subscriptionJson);

        logger.info("****************");
        logger.info("PayloadHash: "+payloadHash);
        logger.info("SubscriptionHash: "+subscriptionHash);
        logger.info("****************");

        assertEquals(subscriptionId, subscription.getSubscriptionId());
        assertEquals(payloadHash, subscriptionHash);

        //update subscription
        Pipe pipe = new Pipe();
        String pipeId = UUID.randomUUID().toString();
        String pipeName = "pipeName";
        pipe.setPipeId(pipeId);
        pipe.setPipeName(pipeName);
        subscription.setPipe(pipe);
        endpoint = "/subscription_manager/update_subscription/";
        responseJson = ApiUtil.apiPostRequest(endpoint,subscription.toString(),securityToken).getAsJsonObject();

        subscriptionJson = responseJson.getAsJsonObject("subscription");
        subscription = Subscription.parse(subscriptionJson.toString());

        subscriptionHash = JsonUtil.getJsonHash(subscriptionJson);

        logger.info("****************");
        logger.info("PayloadHash: "+payloadHash);
        logger.info("SubscriptionHash: "+subscriptionHash);
        logger.info("****************");

        assertEquals(subscriptionId, subscription.getSubscriptionId());
        assertNotEquals(payloadHash, subscriptionHash);
    }

    @Test
    public void deleteSubscription() throws Exception {
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

    @Test
    public void deleteSubscriptionEndpoint() throws Exception {
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
            logger.info("HASH: " + hash);
            logger.info("**********************");
        }

        logger.info("*****************************************************************");
        for(String subscriptionId: subscriptionIds) {
            String endpoint = "/subscription_manager/delete_subscription/"+subscriptionId+"/";
            SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
            JsonObject responseJson = ApiUtil.apiDeleteRequest(endpoint,securityToken).getAsJsonObject();
            String deleted = responseJson.get("subscriptionId").getAsString();

            Subscription subscription = this.subscriptionService.getSubscription(
                    subscriptionId);

            assertEquals(deleted, subscriptionId);
            assertNull(subscription);
        }
    }
}
