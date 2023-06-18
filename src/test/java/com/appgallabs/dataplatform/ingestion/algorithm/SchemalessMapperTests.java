package com.appgallabs.dataplatform.ingestion.algorithm;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SchemalessMapperTests {
    private static Logger logger = LoggerFactory.getLogger(SchemalessMapperTests.class);

    @Inject
    private SchemalessMapper schemalessMapper;

    @Test
    public void mapAll() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/subset.json"),
                StandardCharsets.UTF_8
        );

        Map<String,Object> flatJson = this.schemalessMapper.mapAll(jsonString);

        String nestedJson = JsonUnflattener.unflatten(flatJson.toString());
        JsonUtil.printStdOut(JsonParser.parseString(nestedJson));

        JsonObject inputJson = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonObject ingestedJson = JsonParser.parseString(nestedJson).getAsJsonObject();

        assertEquals(JsonUtil.getJsonHash(inputJson),JsonUtil.getJsonHash(ingestedJson));
    }

    @Test
    public void mapSubset() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/subset.json"),
                StandardCharsets.UTF_8
        );

        List<String> queries = Arrays.asList(
                "$.store.book[?(@.price != 0)]"
        );

        Map<String,Object> flatJson = this.schemalessMapper.mapSubset(jsonString,queries);

        Gson gson = new Gson();
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);
        ReadContext readContext = JsonPath.parse(document);

        Map<String,Object> read = readContext.read("$.store");

        String readJson = gson.toJson(read, LinkedHashMap.class);
        String inputJsonString = JsonUnflattener.unflatten(readJson);
        JsonObject inputJson = JsonParser.parseString(inputJsonString).getAsJsonObject();
        JsonArray input = inputJson.getAsJsonArray("book");

        JsonUtil.printStdOut(input);
        System.out.println("*************************************");

        String flattenedJsonString = gson.toJson(flatJson, LinkedHashMap.class);
        String ingestedJsonString = JsonUnflattener.unflatten(flattenedJsonString);
        JsonObject ingestedJson = JsonParser.parseString(ingestedJsonString).getAsJsonObject();
        JsonArray ingested = ingestedJson.getAsJsonObject("store").getAsJsonArray("book");

        JsonUtil.printStdOut(ingested);

        assertEquals(JsonUtil.getJsonHash(input),JsonUtil.getJsonHash(ingested));
    }
}
