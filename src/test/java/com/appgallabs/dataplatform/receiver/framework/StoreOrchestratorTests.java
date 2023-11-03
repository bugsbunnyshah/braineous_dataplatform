package com.appgallabs.dataplatform.receiver.framework;

import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.MongoDBUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import test.components.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreOrchestratorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(StoreOrchestratorTests.class);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        String jsonString = Util.loadResource("receiver/mongodb_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void receiveData() throws Exception{
        List<String> objectHashes = new ArrayList<>();
        String jsonString = Util.loadResource("receiver/dataset.json");
        JsonArray dataSetArray = JsonUtil.validateJson(jsonString).getAsJsonArray();
        for(int i=0; i<dataSetArray.size(); i++){
            JsonObject dataObjectJson = dataSetArray.get(i).getAsJsonObject();
            objectHashes.add(JsonUtil.getJsonHash(dataObjectJson));
        }

        StoreOrchestrator storeOrchestrator = StoreOrchestrator.getInstance();
        storeOrchestrator.receiveData("123", jsonString);

        //assert
        Registry registry = Registry.getInstance();
        JsonArray driverConfigurations = registry.getDriverConfigurations();
        JsonArray storeConfiguration = driverConfigurations.get(0).getAsJsonArray();
        JsonObject driverConfiguration = storeConfiguration.get(0).getAsJsonObject().getAsJsonObject("config");
        String connectionString = driverConfiguration.get("connectionString").getAsString();
        String database = driverConfiguration.get("database").getAsString();
        String collection = driverConfiguration.get("collection").getAsString();
        Set<String> collectionHashes = MongoDBUtil.readCollectionHashes(connectionString,
                database,collection);
        JsonUtil.printStdOut(JsonUtil.validateJson(objectHashes.toString()));

        for(String hash:objectHashes){
            assertTrue(collectionHashes.contains(hash));
        }
    }
}
