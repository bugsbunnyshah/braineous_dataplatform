package com.appgallabs.dataplatform.ingestion.algorithm;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
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
}
