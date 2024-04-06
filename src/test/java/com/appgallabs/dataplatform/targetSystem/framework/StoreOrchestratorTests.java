package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.staging.InMemoryDB;
import com.appgallabs.dataplatform.targetSystem.framework.staging.InMemoryStagingStore;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class StoreOrchestratorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(StoreOrchestratorTests.class);

    private static String pipeConf = "pipeline/pipeline_config_1.json";
    private static String multiplePipeConf = "pipeline/pipeline_config_multiple.json";

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SchemalessMapper schemalessMapper;

    @Inject
    private PipelineService pipelineService;

    @Inject
    private StoreOrchestrator storeOrchestrator;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        InMemoryDB.getInstance().clear();
        this.storeOrchestrator.runInDevMode();
    }

    //@Test
    public void receiveDataSinglePipe() throws Exception{
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        Tenant tenant = new Tenant(securityToken.getPrincipal());

        String pipeConfigString = Util.loadResource(pipeConf);
        JsonObject pipeConfig = JsonUtil.validateJson(pipeConfigString).getAsJsonObject();
        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, pipeConfig);

        String pipeId = pipeConfig.get("pipeId").getAsString();
        String entity = pipeConfig.get("entity").getAsString();
        long offset = 0l;

        SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();

        List<String> objectHashes = new ArrayList<>();
        String jsonString = Util.loadResource("receiver/input_array.json");
        JsonArray dataSetArray = JsonUtil.validateJson(jsonString).getAsJsonArray();
        for(int i=0; i<dataSetArray.size(); i++){
            JsonObject dataObjectJson = dataSetArray.get(i).getAsJsonObject();
            objectHashes.add(JsonUtil.getJsonHash(dataObjectJson));
        }

        this.storeOrchestrator.receiveData(securityToken,
                systemStore,
                this.schemalessMapper,
                pipeId,
                offset,
                entity,
                jsonString);

        //assert
        String key = InMemoryStagingStore.getKey(tenant, pipeId, entity);
        InMemoryDB stagingStoreDb = InMemoryDB.getInstance();
        List<Record> stored = stagingStoreDb.getRecordStore().get(key);
        JsonUtil.printStdOut(JsonUtil.validateJson(stored.toString()));
        assertEquals(2, stored.size());
        assertEquals("B48FD0E8E6EABFF5B595D559A7B15D2D" , stored.get(0).getRecordMetaData()
                .getMetadata()
                .get("objectHash").getAsString());
        assertEquals("F64EFF58513BC7472380954E4A16E0D4" , stored.get(1).getRecordMetaData()
                .getMetadata()
                .get("objectHash").getAsString());

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

    //@Test
    public void receiveDataMultiplePipes() throws Exception{
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        Tenant tenant = new Tenant(securityToken.getPrincipal());

        String pipeConfigString = Util.loadResource(multiplePipeConf);
        JsonObject pipeConfig = JsonUtil.validateJson(pipeConfigString).getAsJsonObject();
        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, pipeConfig);

        String pipeId = pipeConfig.get("pipeId").getAsString();
        String entity = pipeConfig.get("entity").getAsString();
        long offset = 0l;

        SystemStore systemStore = this.mongoDBJsonStore.getSystemStore();

        List<String> objectHashes = new ArrayList<>();
        String jsonString = Util.loadResource("receiver/input_array.json");
        JsonArray dataSetArray = JsonUtil.validateJson(jsonString).getAsJsonArray();
        for(int i=0; i<dataSetArray.size(); i++){
            JsonObject dataObjectJson = dataSetArray.get(i).getAsJsonObject();
            objectHashes.add(JsonUtil.getJsonHash(dataObjectJson));
        }


        this.storeOrchestrator.receiveData(securityToken,
                systemStore,
                this.schemalessMapper,
                pipeId,
                offset,
                entity,
                jsonString);

        //assert
        String key = InMemoryStagingStore.getKey(tenant, pipeId, entity);
        InMemoryDB stagingStoreDb = InMemoryDB.getInstance();
        List<Record> stored = stagingStoreDb.getRecordStore().get(key);
        JsonUtil.printStdOut(JsonUtil.validateJson(stored.toString()));
        assertEquals(4, stored.size());
        assertEquals("B48FD0E8E6EABFF5B595D559A7B15D2D" , stored.get(0).getRecordMetaData()
                .getMetadata()
                .get("objectHash").getAsString());
        assertEquals("F64EFF58513BC7472380954E4A16E0D4" , stored.get(1).getRecordMetaData()
                .getMetadata()
                .get("objectHash").getAsString());

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
