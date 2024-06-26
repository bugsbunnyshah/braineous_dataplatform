package com.appgallabs.dataplatform.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.RegistryStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.manager.InvalidPipeIdException;
import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscriber;
import com.appgallabs.dataplatform.pipeline.manager.model.SubscriberGroup;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.pipeline.manager.service.SubscriptionService;
import com.appgallabs.dataplatform.pipeline.manager.util.ValidationUtil;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;

import java.util.*;

public class Registry {
    private static Logger logger = LoggerFactory.getLogger(Registry.class);

    private static Registry singleton = new Registry();

    private MongoDBJsonStore mongoDBJsonStore;

    private SubscriptionService subscriptionService;

    private JsonObject datalakeConfiguration;

    private Registry() {
        try {
            //find from the Quarkus registry
            this.mongoDBJsonStore = CDI.current().select(MongoDBJsonStore.class).get();
            this.subscriptionService = CDI.current().select(SubscriptionService.class).get();

            String jsonString = Util.loadResource("datalake/datalake_config.json");
            this.datalakeConfiguration = JsonUtil.validateJson(jsonString).getAsJsonObject();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Registry getInstance(){
        //safe-check, cause why not
        if(Registry.singleton == null){
            Registry.singleton = new Registry();
        }
        return Registry.singleton;
    }

    public JsonObject getDatalakeConfiguration() {
        return datalakeConfiguration;
    }


    //read operations---------------------------------------------------------
    public List<StagingStore> findStagingStores(String tenant, String pipeId){
        try {
            List<StagingStore> result = new ArrayList<>();
            MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
            RegistryStore registryStore = this.mongoDBJsonStore.getRegistryStore();

            JsonArray jsonArray = registryStore.findStagingStores(
                tenant,
                mongoClient,
                pipeId
            );

            if (jsonArray == null || jsonArray.size() == 0) {
                return result;
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject configurationJson = jsonArray.get(i).getAsJsonObject();
                JsonObject storeConfigJson = configurationJson.getAsJsonObject("config");

                String storeDriverClass = configurationJson.get("stagingStore").getAsString();
                StagingStore stagingStore = (StagingStore) Thread.currentThread().getContextClassLoader().
                        loadClass(storeDriverClass).getDeclaredConstructor().newInstance();

                stagingStore.configure(storeConfigJson);

                result.add(stagingStore);
            }

            return result;
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    //write operations------------------------------------------------------------------
    public String registerPipe(Tenant tenant, JsonObject pipeRegistration)
    throws InvalidPipeIdException
    {
        String principal = tenant.getPrincipal();

        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        RegistryStore registryStore = this.mongoDBJsonStore.getRegistryStore();
        String pipeId = pipeRegistration.get("pipeId").getAsString();

        //validate pipe id specification
        ValidationUtil.validatePipeId(pipeId);

        //persist
        registryStore.registerPipe(
                tenant,
                mongoClient,
                pipeRegistration
        );

        //create a subscription
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber(principal));
        Pipe newPipe = new Pipe(pipeId,pipeId);
        Subscription subscription = new Subscription(UUID.randomUUID().toString(), group,
                newPipe);
        this.subscriptionService.createSubscription(subscription);

        return pipeId;
    }
}
