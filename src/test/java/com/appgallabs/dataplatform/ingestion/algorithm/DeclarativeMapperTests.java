package com.appgallabs.dataplatform.ingestion.algorithm;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DeclarativeMapperTests {
    private static Logger logger = LoggerFactory.getLogger(DeclarativeMapperTests.class);

    @Test
    public void mapAll() throws Exception {
        String jsonString = IOUtils.toString(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("ingestion/algorithm/mapAll.json"),
                StandardCharsets.UTF_8
        );

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonString);
        JsonUtil.printStdOut(JsonParser.parseString(flattenJson.toString()));

        String nestedJson = JsonUnflattener.unflatten(flattenJson.toString());
        JsonUtil.printStdOut(JsonParser.parseString(nestedJson));
    }

    @Test
    public void processJsonSubset() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/mapAll.json"),
                StandardCharsets.UTF_8
        );

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonUtil.printStdOut(json);
    }

    @Test
    public void prototypeJsonPath() throws Exception{

    }
}
