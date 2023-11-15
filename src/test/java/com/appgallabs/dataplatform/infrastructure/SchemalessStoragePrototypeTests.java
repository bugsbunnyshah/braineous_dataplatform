package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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

@QuarkusTest
public class SchemalessStoragePrototypeTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(SchemalessStoragePrototypeTests.class);

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void prototypeStoreFlatJson() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonString);
        JsonUtil.printStdOut(JsonParser.parseString(flattenJson.toString()));

        Tenant tenant = this.securityTokenContainer.getTenant();
        System.out.println(tenant.getPrincipal());

        //Store the FlatJson
        String dataLakeId = this.mongoDBJsonStore.storeIngestion(tenant,flattenJson);

        //Read the Json
        JsonArray result = this.mongoDBJsonStore.readIngestion(tenant,dataLakeId);
        JsonUtil.printStdOut(result);
    }
}
