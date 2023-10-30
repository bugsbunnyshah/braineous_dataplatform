package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.components.BaseTest;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void ingestArray() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(1).getAsJsonObject();
        String originalObjectHash = JsonUtil.getJsonHash(jsonObject);
        jsonObject.remove("expensive");
        String compareLeftObjectHash = JsonUtil.getJsonHash(jsonObject);

        String entity = TempConstants.ENTITY;
        this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(), entity,jsonString);

        JsonArray ingestion = this.mongoDBJsonStore.readIngestion(this.securityTokenContainer.getTenant(),
                originalObjectHash);

        JsonObject storedJson = ingestion.get(0).getAsJsonObject();
        JsonUtil.printStdOut(storedJson);

        storedJson.remove("expensive");
        String compareRightObjectHash = JsonUtil.getJsonHash(storedJson);

        System.out.println("*************");
        System.out.println(compareRightObjectHash);
        System.out.println("*************");
        assertEquals(compareLeftObjectHash,compareRightObjectHash);
    }

    @Test
    public void ingestObject() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String originalObjectHash = JsonUtil.getJsonHash(jsonObject);
        jsonObject.remove("expensive");
        String compareLeftObjectHash = JsonUtil.getJsonHash(jsonObject);

        String entity = TempConstants.ENTITY;
        this.pipelineService.ingest(this.securityTokenContainer.getSecurityToken(),entity,jsonString);

        JsonArray ingestion = this.mongoDBJsonStore.readIngestion(this.securityTokenContainer.getTenant(),
                originalObjectHash);

        JsonObject storedJson = ingestion.get(0).getAsJsonObject();
        JsonUtil.printStdOut(storedJson);

        storedJson.remove("expensive");
        String compareRightObjectHash = JsonUtil.getJsonHash(storedJson);

        System.out.println("*************");
        System.out.println(compareRightObjectHash);
        System.out.println("*************");
        assertEquals(compareLeftObjectHash,compareRightObjectHash);
    }
}
