package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.client.sdk.api.Configuration;
import com.appgallabs.dataplatform.client.sdk.api.DataPipeline;
import com.appgallabs.dataplatform.pipeline.manager.model.*;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.ApiUtil;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
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

    @Inject
    private SecurityTokenContainer securityTokenContainer;

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
            SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
            JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody,securityToken);
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
            SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
            JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody,securityToken);
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
            SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
            JsonElement responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody,securityToken);
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
    public void devPipes() throws Exception{
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);


            subscription.getPipe().setPipeStage(PipeStage.DEVELOPMENT);

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

        JsonArray pipes = this.pipeService.devPipes();
        JsonUtil.printStdOut(pipes);
        assertEquals(2, pipes.size());
    }

    @Test
    public void devPipesEndpoint() throws Exception{
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);


            subscription.getPipe().setPipeStage(PipeStage.DEVELOPMENT);

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

        String endpoint = "/pipeline_manager/dev_pipes/";
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonArray pipes = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonArray();
        JsonUtil.printStdOut(pipes);
        assertEquals(2, pipes.size());
    }

    @Test
    public void stagedPipesEndpoint() throws Exception{
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        List<String> subscriptionIds = new ArrayList<>();
        List<String> subscriptionHashes = new ArrayList<>();
        for(int i=0; i<group.getSubscribers().size(); i++) {
            Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");
            String subscriptionId = UUID.randomUUID().toString();
            Subscription subscription = new Subscription(subscriptionId, group, pushPipe);


            subscription.getPipe().setPipeStage(PipeStage.STAGED);

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

        String endpoint = "/pipeline_manager/staged_pipes/";
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonArray pipes = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonArray();
        JsonUtil.printStdOut(pipes);
        assertEquals(2, pipes.size());
    }

    @Test
    public void deployedPipesEndpoint() throws Exception{
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

        String endpoint = "/pipeline_manager/deployed_pipes/";
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonArray pipes = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonArray();
        JsonUtil.printStdOut(pipes);
        assertEquals(2, pipes.size());
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
        JsonArray liveSnapShot = this.pipeService.getLiveSnapShot(clientIp,
                snapshotId,
                livePipe.getPipeName());
        JsonUtil.printStdOut(liveSnapShot);
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
        payload.addProperty("pipeName", livePipe.getPipeName());
        String jsonBody = payload.toString();

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();

        JsonArray responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody,securityToken).getAsJsonArray();
        JsonUtil.printStdOut(responseJson);
    }

    @Test
    public void getLiveSnapShot404Endpoint()
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
        payload.addProperty("pipeName", "blah");
        String jsonBody = payload.toString();
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody,securityToken).getAsJsonObject();
        JsonUtil.printStdOut(responseJson);

        //assert
        String exception = responseJson.get("exception").getAsString();
        String expected = "PIPE_NOT_FOUND";
        assertEquals(exception, expected);
    }

    @Test
    public void ingestionStatsEndpoint()
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

        String pipeId = livePipe.getPipeId();
        String pipeName = livePipe.getPipeName();
        String endpoint = "/pipeline_manager/ingestion_stats/"+pipeName+"/";;
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonObject();
        JsonUtil.printStdOut(responseJson);

        //asserts
        String type = responseJson.get("type").getAsString();
        String typeExpected = "ingestion";
        assertEquals(type, typeExpected);

        String pipeIdStored = responseJson.get("pipeId").getAsString();
        String pipeIdExpected = pipeId;
        assertEquals(pipeIdStored, pipeIdExpected);

        String pipeNameStored = responseJson.get("pipeName").getAsString();
        String pipeNameExpected = pipeName;
        assertEquals(pipeNameStored, pipeNameExpected);
    }

    @Test
    public void ingestionStats404Endpoint()
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

        String pipeName = "blah";
        String endpoint = "/pipeline_manager/ingestion_stats/"+pipeName+"/";;
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonObject();

        //assert
        String exception = responseJson.get("exception").getAsString();
        String expected = "PIPE_NOT_FOUND";
        assertEquals(exception, expected);
    }

    @Test
    public void deliveryStatsEndpoint()
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

        String pipeId = livePipe.getPipeId();
        String pipeName = livePipe.getPipeName();
        String endpoint = "/pipeline_manager/delivery_stats/"+pipeName+"/";

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiGetRequest(endpoint, securityToken).getAsJsonObject();

        //asserts
        String type = responseJson.get("type").getAsString();
        String typeExpected = "delivery";
        assertEquals(type, typeExpected);

        String pipeIdStored = responseJson.get("pipeId").getAsString();
        String pipeIdExpected = pipeId;
        assertEquals(pipeIdStored, pipeIdExpected);

        String pipeNameStored = responseJson.get("pipeName").getAsString();
        String pipeNameExpected = pipeName;
        assertEquals(pipeNameStored, pipeNameExpected);
    }

    @Test
    public void deliveryStats404Endpoint()
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

        String pipeName = "blah";
        String endpoint = "/pipeline_manager/delivery_stats/"+pipeName+"/";;

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiGetRequest(endpoint, securityToken).getAsJsonObject();

        //assert
        String exception = responseJson.get("exception").getAsString();
        String expected = "PIPE_NOT_FOUND";
        assertEquals(exception, expected);
    }

    //@Test
    public void endToEnd() throws Exception{
        //create a pipe
        String pipeName = "medical_records";
        JsonObject pipeCreateResponse = this.createPipe(pipeName);
        String pipeId = pipeCreateResponse.getAsJsonObject("pipe").
                get("pipeId").
                getAsString();

        String message = pipeCreateResponse.get("message").getAsString();
        assertEquals(message, "PIPE_SUCCESSFULLY_REGISTERED");

        //send data to pipe via ingestion
        JsonObject sendDataResponse = this.sendData(pipeId,pipeName);
        JsonUtil.printStdOut(sendDataResponse);

        logger.info("WAITING_ON_INGESTION_TO_COMPLETE........");
        Thread.sleep(15000);

        //get live feed
        JsonArray liveFeed = this.getLiveFeed(pipeName);
        logger.info("LIVE_FEED_SIZE: "+liveFeed.size());

        //get ingestion stats
        JsonObject ingestionStats = this.getIngestionStats(pipeName);

        //get delivery stats
        JsonObject deliveryStats = this.getDeliveryStats(pipeName);
    }

    //@Test
    public void endToEndReadOnly() throws Exception{
        //create a pipe
        String pipeName = "medical_records";

        //get live feed
        JsonArray liveFeed = this.getLiveFeed(pipeName);
        logger.info("LIVE_FEED_SIZE: "+liveFeed.size());

        //get ingestion stats
        JsonObject ingestionStats = this.getIngestionStats(pipeName);

        //get delivery stats
        JsonObject deliveryStats = this.getDeliveryStats(pipeName);
    }

    private JsonObject createPipe(String pipeName){
        String endpoint = "/pipeline_manager/move_to_development/";

        JsonObject payload = new JsonObject();
        payload.addProperty("pipeName", pipeName);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiPostRequest(endpoint,payload.toString(),securityToken)
                .getAsJsonObject();

        return responseJson;
    }

    private JsonObject sendData(String pipeId, String pipeName){
        try {
            //configure the DataPipeline Client
            //configure the DataPipeline Client
            Configuration configuration = new Configuration().
                    streamSizeInBytes(80).
                    ingestionHostUrl("http://localhost:8080/");
            DataPipeline.configure(configuration);

            String datasetLocation = "tutorial/usecase/scenario1/scenario1Array.json";
            String json = Util.loadResource(datasetLocation);
            JsonElement datasetElement = JsonUtil.validateJson(json);

            //register a pipeline
            String configLocation = "tutorial/usecase/scenario1/single_store_pipe_config.json";
            json = Util.loadResource(configLocation);
            JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();
            configJson.addProperty("pipeId", pipeId);
            JsonObject registrationJson = DataPipeline.registerPipe(configJson.toString());

            //send source data through the pipeline
            String entity = pipeName;
            DataPipeline.sendData(pipeId, entity,datasetElement.toString());

            return registrationJson;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private JsonArray getLiveFeed(String pipeName){
        String endpoint = "/pipeline_manager/live_snapshot/";;
        String clientIp = "127.00.1";
        String snapshotId = UUID.randomUUID().toString();

        JsonObject payload = new JsonObject();
        payload.addProperty("clientIp", clientIp);
        payload.addProperty("snapshotId", snapshotId);
        payload.addProperty("pipeName", pipeName);
        String jsonBody = payload.toString();
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonArray responseJson = ApiUtil.apiPostRequest(endpoint,jsonBody, securityToken).getAsJsonArray();

        return responseJson;
    }

    private JsonObject getIngestionStats(String pipeName){
        String endpoint = "/pipeline_manager/ingestion_stats/"+pipeName+"/";
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonObject();
        return responseJson;
    }

    private JsonObject getDeliveryStats(String pipeName){
        String endpoint = "/pipeline_manager/delivery_stats/"+pipeName+"/";
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        JsonObject responseJson = ApiUtil.apiGetRequest(endpoint,securityToken).getAsJsonObject();
        return responseJson;
    }
}
