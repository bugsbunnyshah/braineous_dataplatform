package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.staging.InMemoryStagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.components.BaseTest;
import test.components.Util;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class StoreOrchestratorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(StoreOrchestratorTests.class);

    private static String pipeConf = "pipeline/pipeline_config_1.json";
    //private static String pipeConf = "pipeline/pipeline_config_multiple.json";

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private PipelineService pipelineService;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        InMemoryStagingStore.getInstance().clear();

        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = Util.loadResource(pipeConf);

        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void receiveData() throws Exception{
        String pipeConfigString = Util.loadResource(pipeConf);
        JsonObject pipeConfig = JsonUtil.validateJson(pipeConfigString).getAsJsonObject();

        String pipeId = pipeConfig.get("pipeId").getAsString();
        String entity = pipeConfig.get("entity").getAsString();

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();

        SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();

        List<String> objectHashes = new ArrayList<>();
        String jsonString = Util.loadResource("receiver/input_array.json");
        JsonArray dataSetArray = JsonUtil.validateJson(jsonString).getAsJsonArray();
        for(int i=0; i<dataSetArray.size(); i++){
            JsonObject dataObjectJson = dataSetArray.get(i).getAsJsonObject();
            objectHashes.add(JsonUtil.getJsonHash(dataObjectJson));
        }

        StoreOrchestrator storeOrchestrator = StoreOrchestrator.getInstance();
        storeOrchestrator.receiveData(securityToken,
                systemStore,
                this.schemalessMapper,
                pipeId,
                entity,
                jsonString);

        Thread.sleep(5000);
        //while(true);

        //TODO: (CR2)
        //assert
        /*
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
        }*/
    }
}
