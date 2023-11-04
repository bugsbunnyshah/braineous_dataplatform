package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStoreTests;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DataLakeDriverTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataLakeDriverTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    //@Test
    public void storeFlatJson() throws Exception{
        //TODO (NOW)
        /*DataLakeDriver dataLakeDriver = null;

        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonString);
        JsonUtil.printStdOut(JsonParser.parseString(flattenJson.toString()));

        Tenant tenant = this.securityTokenContainer.getTenant();

        //Store the FlatJson
        //String dataLakeId = dataLakeDriver.storeIngestion(tenant,flattenJson);

        //Read the Json
        JsonArray result = dataLakeDriver.readIngestion(tenant,dataLakeId);
        JsonUtil.printStdOut(result);

        JsonObject inputJson = JsonParser.parseString(jsonString).getAsJsonObject();
        inputJson.remove("expensive");
        String inputHash = JsonUtil.getJsonHash(inputJson);

        JsonObject resultJson = result.get(0).getAsJsonObject();
        resultJson.remove("expensive");
        String resultHash = JsonUtil.getJsonHash(resultJson);

        assertEquals(inputHash, resultHash);*/
    }
}
