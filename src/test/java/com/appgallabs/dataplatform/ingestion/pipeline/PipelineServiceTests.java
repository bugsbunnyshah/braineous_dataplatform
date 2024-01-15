package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.TestConstants;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.components.BaseTest;
import test.components.Util;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class PipelineServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(PipelineServiceTests.class);

    @Inject
    private PipelineService pipelineService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = Util.loadResource("pipeline/mongodb_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void ingestArray() throws Exception{
        String originalObjectHash = null;
        try {
            String pipeId = "123";

            String jsonString = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().getResourceAsStream("ingestion/pipeline/obj1_array.json"),
                    StandardCharsets.UTF_8
            );
            JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();
            JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
            originalObjectHash = JsonUtil.getJsonHash(jsonObject);

            String entity = TestConstants.ENTITY;
            JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();
            this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(),
                    datalakeDriverConfiguration.toString(),
                    pipeId, 0,
                    entity, jsonString);

            /*

            JsonArray ingestion = this.mongoDBJsonStore.readIngestion(this.securityTokenContainer.getTenant(),
                    originalObjectHash);

            JsonObject storedJson = ingestion.get(0).getAsJsonObject();
            JsonUtil.printStdOut(storedJson);

            storedJson.remove("expensive");
            String compareRightObjectHash = JsonUtil.getJsonHash(storedJson);

            System.out.println("*************");
            System.out.println(compareRightObjectHash);
            System.out.println("*************");
            assertEquals(compareLeftObjectHash, compareRightObjectHash);

             */
        }finally {
            System.out.println(originalObjectHash);
        }
    }

    @Test
    public void ingestObject() throws Exception{
        String originalObjectHash = null;
        try {
            String pipeId = "123";

            String jsonString = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().getResourceAsStream("ingestion/pipeline/obj1.json"),
                    StandardCharsets.UTF_8
            );
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            originalObjectHash = JsonUtil.getJsonHash(jsonObject);

            String entity = TestConstants.ENTITY;
            JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();
            this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(),
                    datalakeDriverConfiguration.toString(),
                    pipeId, 0,
                    entity, jsonString);

            /*
            //Thread.sleep(30000l);

            JsonArray ingestion = this.mongoDBJsonStore.readIngestion(this.securityTokenContainer.getTenant(),
                    originalObjectHash);

            JsonObject storedJson = ingestion.get(0).getAsJsonObject();
            JsonUtil.printStdOut(storedJson);

            String compareRightObjectHash = JsonUtil.getJsonHash(storedJson);

            System.out.println("*************");
            System.out.println(compareRightObjectHash);
            System.out.println("*************");
            assertEquals(compareLeftObjectHash, compareRightObjectHash);*/
        }finally{
            System.out.println(originalObjectHash);
        }
    }
}
