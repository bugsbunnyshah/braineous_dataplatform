package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.*;
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
public class PipeServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(PipeServiceTests.class);

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private PipeService pipeService;

    @Test
    public void moveToDevelopment() throws Exception
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


            subscription.getPipe().setPipeStage(PipeStage.DEPLOYED);

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
            Pipe updated = this.pipeService.moveToDevelopment(subscription.getPipe());
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEPLOYED);
            assertEquals(updated.getPipeStage(), PipeStage.DEVELOPMENT);
        }
    }

    @Test
    public void moveToDevelopmentEndpoint() throws Exception
    {
        String endpoint = "/pipeline_manager/move_to_development/";

        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);


            subscription.getPipe().setPipeStage(PipeStage.DEPLOYED);

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
            String jsonBody = old.toString();
            JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody);
            JsonObject updatedJson = responseJson.getAsJsonObject().get("pipe").getAsJsonObject();
            Pipe updated = Pipe.parse(updatedJson.toString());

            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEPLOYED);
            assertEquals(updated.getPipeStage(), PipeStage.DEVELOPMENT);
        }
    }

    @Test
    public void moveToStagedEndpoint() throws Exception{
        String endpoint = "/pipeline_manager/move_to_staged/";

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
            //update the pipe
            String jsonBody = old.toString();
            JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody);
            JsonObject updatedJson = responseJson.getAsJsonObject().get("pipe").getAsJsonObject();
            Pipe updated = Pipe.parse(updatedJson.toString());

            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEVELOPMENT);
            assertEquals(updated.getPipeStage(), PipeStage.STAGED);
        }
    }

    @Test
    public void moveToStaged() throws Exception{
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
            Pipe updated = this.pipeService.moveToStaged(subscription.getPipe());
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEVELOPMENT);
            assertEquals(updated.getPipeStage(), PipeStage.STAGED);
        }
    }

    @Test
    public void moveToDeployed() throws Exception {
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
            Pipe updated = this.pipeService.moveToDeployed(subscription.getPipe());
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEVELOPMENT);
            assertEquals(updated.getPipeStage(), PipeStage.DEPLOYED);
        }
    }

    @Test
    public void moveToDeployedEndpoint() throws Exception {
        String endpoint = "/pipeline_manager/move_to_deployed/";
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
            String jsonBody = old.toString();
            JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody);
            JsonObject updatedJson = responseJson.getAsJsonObject().get("pipe").getAsJsonObject();
            Pipe updated = Pipe.parse(updatedJson.toString());

            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEVELOPMENT);
            assertEquals(updated.getPipeStage(), PipeStage.DEPLOYED);
        }
    }

    @Test
    public void getLiveSnapShot()
    throws Exception{
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
        Pipe livePipe = null;
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            Pipe old = subscription.getPipe();

            //update the pipe
            Pipe updated = this.pipeService.moveToDeployed(subscription.getPipe());
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEVELOPMENT);
            assertEquals(updated.getPipeStage(), PipeStage.DEPLOYED);

            livePipe = updated;

            break;
        }

        String clientIp = "127.00.1";
        String snapshotId = UUID.randomUUID().toString();
        List<String> liveSnapShot = this.pipeService.getLiveSnapShot(clientIp,
                snapshotId,
                livePipe);
        JsonUtil.printStdOut(JsonUtil.validateJson(liveSnapShot.toString()));
    }

    @Test
    public void getLiveSnapShotEndpoint()
            throws Exception{
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
        Pipe livePipe = null;
        for(String subscriptionId: subscriptionIds) {
            //Get all subscriptions
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
            String hash = JsonUtil.getJsonHash(subscription.toJson());
            Pipe old = subscription.getPipe();

            //update the pipe
            Pipe updated = this.pipeService.moveToDeployed(subscription.getPipe());
            String newHash = JsonUtil.getJsonHash(updated.toJson());
            subscriptionHashes.remove(hash);
            subscriptionHashes.add(newHash);

            logger.info("****SUBSCRIPTION_ID_HASH_UPDATED***");
            logger.info("ID: " +updated);
            logger.info("PipeStageBefore: " +old.getPipeStage());
            logger.info("PipeStageAfter: " +updated.getPipeStage());
            logger.info("OLD_HASH: " + hash);
            logger.info("NEW_HASH: " + newHash);
            JsonUtil.printStdOut(updated.toJson());
            logger.info("**********************");

            assertNotNull(subscription);
            assertFalse(subscriptionHashes.contains(hash));
            assertTrue(subscriptionHashes.contains(newHash));
            assertEquals(old.getPipeStage(), PipeStage.DEVELOPMENT);
            assertEquals(updated.getPipeStage(), PipeStage.DEPLOYED);

            livePipe = updated;

            break;
        }

        String endpoint = "/pipeline_manager/live_snapshot/";;
        String clientIp = "127.00.1";
        String snapshotId = UUID.randomUUID().toString();

        JsonObject payload = new JsonObject();
        payload.addProperty("clientIp", clientIp);
        payload.addProperty("snapshotId", snapshotId);
        payload.addProperty("pipeId", livePipe.getPipeId());
        String jsonBody = payload.toString();
        JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody);
        List<String> liveSnapShot = this.pipeService.getLiveSnapShot(clientIp,
                snapshotId,
                livePipe);
        JsonUtil.printStdOut(JsonUtil.validateJson(liveSnapShot.toString()));
    }
}
