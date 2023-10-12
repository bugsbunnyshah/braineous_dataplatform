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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
