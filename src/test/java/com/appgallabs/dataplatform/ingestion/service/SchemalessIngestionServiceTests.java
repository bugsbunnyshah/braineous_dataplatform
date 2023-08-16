package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@QuarkusTest
public class SchemalessIngestionServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(SchemalessIngestionServiceTests.class);

    @Inject
    private SchemalessIngestionService schemalessIngestionService;

    @Inject
    Instance<DataLakeDriver> dataLakeDriverInstance;

    //@Test
    public void processFull() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
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

    //@Test
    public void processSubset() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        List<String> jsonPathExpressions = Arrays.asList("$.store.book[?(@.price != 0)]");

        String datalakeId = this.schemalessIngestionService.processSubset(jsonString,jsonPathExpressions);

        JsonArray result = this.schemalessIngestionService.readIngestion(datalakeId);

        String inputSubsetString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/pulled.json"),
                StandardCharsets.UTF_8
        );
        JsonObject inputJson = JsonParser.parseString(inputSubsetString).getAsJsonObject();
        String inputHash = JsonUtil.getJsonHash(inputJson);

        JsonObject resultJson = result.get(0).getAsJsonObject();
        String resultHash = JsonUtil.getJsonHash(resultJson);

        assertEquals(inputHash, resultHash);
    }

    //@Test
    public void processNestedArrays() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_nested_arrays.json"),
                StandardCharsets.UTF_8
        );
        JsonElement originalJson = JsonParser.parseString(jsonString);
        System.out.println("***ORIGINAL***");
        JsonUtil.printStdOut(originalJson);

        List<String> jsonPathExpressions = Arrays.asList("$.store.book[?(@.price != 0)]");

        String datalakeId = this.schemalessIngestionService.processSubset(jsonString,jsonPathExpressions);

        JsonArray result = this.schemalessIngestionService.readIngestion(datalakeId);

        String inputSubsetString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/pulled_nested_arrays.json"),
                StandardCharsets.UTF_8
        );
        JsonObject inputJson = JsonParser.parseString(inputSubsetString).getAsJsonObject();
        JsonUtil.printStdOut(inputJson);
        String inputHash = JsonUtil.getJsonHash(inputJson);

        JsonObject resultJson = result.get(0).getAsJsonObject();
        JsonUtil.printStdOut(resultJson);
        String resultHash = JsonUtil.getJsonHash(resultJson);

        assertEquals(inputHash, resultHash);
    }

    //@Test
    public void prototypeInterfaceInjection() throws Exception{
        Config config = ConfigProvider.getConfig();
        String dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        DataLakeDriver dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of(dataLakeDriverName)).get();

        System.out.println("********");
        System.out.println(dataLakeDriver.name());
        System.out.println("********");
    }
}
