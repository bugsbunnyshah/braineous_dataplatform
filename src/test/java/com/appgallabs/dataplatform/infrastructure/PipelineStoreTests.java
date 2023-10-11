package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.mongodb.client.MongoClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class PipelineStoreTests extends BaseTest {
    private Logger logger = LoggerFactory.getLogger(PipelineStoreTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;


    @Test
    public void getAllSubscriptions() throws Exception{
        PipelineStore pipelineStore = this.mongoDBJsonStore.getPipelineStore();
        Tenant tenant = this.securityTokenContainer.getTenant();
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();

        //Get all subscriptions
        List<Subscription> all = pipelineStore.getAllSubscriptions(tenant, mongoClient);

        assertNotNull(all);
        assertTrue(!all.isEmpty());
    }
}
