package com.appgallabs.dataplatform.pipeline;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.RegistryStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.receiver.framework.StoreDriver;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mongodb.client.MongoClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.*;

//TODO: persist Registry (CR1)
public class Registry {
    private static Logger logger = LoggerFactory.getLogger(Registry.class);

    private static Registry singleton = new Registry();

    private MongoDBJsonStore mongoDBJsonStore;

    private JsonObject datalakeConfiguration;
    private Map<String, JsonArray> registry; //pipeId -> StoreDriver

    private Registry() {
        try {
            this.registry = new HashMap<>();

            //find from the Quarkus registry
            this.mongoDBJsonStore = CDI.current().select(MongoDBJsonStore.class).get();

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

    public Map<String, JsonArray> getRegistry() {
        return registry;
    }

    public JsonObject getDatalakeConfiguration() {
        return datalakeConfiguration;
    }


    //read operations---------------------------------------------------------
    public Set<String> allRegisteredPipeIds(){
        Collection<String> keys = this.registry.keySet();

        Set<String> pipeIds = new HashSet<>();
        pipeIds.addAll(keys);

        return pipeIds;
    }

    public List<StoreDriver> findStoreDrivers(String tenant, String pipeId){
        try {
            List<StoreDriver> result = new ArrayList<>();
            MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
            RegistryStore registryStore = this.mongoDBJsonStore.getRegistryStore();

            JsonArray jsonArray = registryStore.findStoreDrivers(
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

                String storeDriverClass = configurationJson.get("storeDriver").getAsString();
                StoreDriver storeDriver = (StoreDriver) Thread.currentThread().getContextClassLoader().
                        loadClass(storeDriverClass).getDeclaredConstructor().newInstance();

                storeDriver.configure(storeConfigJson);

                result.add(storeDriver);
            }

            return result;
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    //write operations------------------------------------------------------------------
    public String registerPipe(Tenant tenant, JsonObject pipeRegistration) {
        MongoClient mongoClient = this.mongoDBJsonStore.getMongoClient();
        RegistryStore registryStore = this.mongoDBJsonStore.getRegistryStore();
        String pipeId = pipeRegistration.get("pipeId").getAsString();
        JsonArray storeDrivers = pipeRegistration.getAsJsonArray("configuration");

        this.registry.put(pipeId, storeDrivers);

        //persist
        registryStore.registerPipe(
                tenant,
                mongoClient,
                pipeRegistration
        );

        return pipeId;
    }

    //TODO: (CR2)
    //----deprecate--------------------------------------------------------------------
    public JsonArray getDriverConfigurations(){
        JsonArray driverConfigurations = new JsonArray();

        Set<Map.Entry<String, JsonArray>> entries = this.registry.entrySet();
        for(Map.Entry<String, JsonArray> entry: entries){
            JsonArray registeredValue = entry.getValue();
            driverConfigurations.add(registeredValue);
        }

        return driverConfigurations;
    }
}
