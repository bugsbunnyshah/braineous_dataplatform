package com.appgallabs.dataplatform.ingestion.service;

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

import static org.junit.jupiter.api.Assertions.assertEquals;


@QuarkusTest
public class SchemalessIngestionServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(SchemalessIngestionServiceTests.class);

    @Inject
    private SchemalessIngestionService schemalessIngestionService;

    @Test
    public void processFull() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/subset.json"),
                StandardCharsets.UTF_8
        );
        String datalakeId = this.schemalessIngestionService.processFull(jsonString);

        //Read the Json
        JsonArray result = this.schemalessIngestionService.readIngestion(datalakeId);

        JsonObject inputJson = JsonParser.parseString(jsonString).getAsJsonObject();
        inputJson.remove("expensive");
        String inputHash = JsonUtil.getJsonHash(inputJson);

        JsonObject resultJson = result.get(0).getAsJsonObject();
        resultJson.remove("expensive");
        String resultHash = JsonUtil.getJsonHash(resultJson);

        assertEquals(inputHash, resultHash);
    }

    @Test
    public void processSubset() throws Exception {

    }
}
