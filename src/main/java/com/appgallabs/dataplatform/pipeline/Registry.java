package com.appgallabs.dataplatform.pipeline;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.receiver.framework.StoreDriver;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

//TODO: persist Registry (CR1)
public class Registry {
    private static Logger logger = LoggerFactory.getLogger(Registry.class);

    private static Registry singleton = new Registry();

    private JsonObject datalakeConfiguration;
    private Map<String, JsonArray> registry; //pipeId -> StoreDriver

    private Map<String, DataLakeDriver> datalakeDrivers;

    private Registry() {
        try {
            this.registry = new HashMap<>();

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

            //TODO: load from db (CR1)
        }
        return Registry.singleton;
    }

    public Map<String, JsonArray> getRegistry() {
        return registry;
    }

    public void setRegistry(Map<String, JsonArray> registry) {
        this.registry = registry;
    }

    public JsonArray findByPipeId(String pipeId){
        return this.registry.get(pipeId);
    }

    public List<StoreDriver> findStoreDrivers(String pipeId){
        try {
            List<StoreDriver> result = new ArrayList<>();

            JsonArray jsonArray = this.registry.get(pipeId);
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

    public String registerPipe(JsonObject pipeRegistration) {

        String pipeId = pipeRegistration.get("pipeId").getAsString();
        JsonArray storeDrivers = pipeRegistration.getAsJsonArray("configuration");

        this.registry.put(pipeId, storeDrivers);

        //TODO: flush to db (CR1)

        return pipeId;
    }

    public Set<String> allRegisteredPipeIds(){
        return this.registry.keySet();
    }

    public JsonArray getDriverConfigurations(){
        JsonArray driverConfigurations = new JsonArray();

        Set<Map.Entry<String, JsonArray>> entries = this.registry.entrySet();
        for(Map.Entry<String, JsonArray> entry: entries){
            JsonArray registeredValue = entry.getValue();
            driverConfigurations.add(registeredValue);
        }

        return driverConfigurations;
    }

    public JsonObject getDatalakeConfiguration() {
        return datalakeConfiguration;
    }
}
