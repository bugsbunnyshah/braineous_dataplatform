package com.appgallabs.dataplatform.receiver.core.driver;

import com.appgallabs.dataplatform.receiver.framework.Configuration;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.Util;

@QuarkusTest
public class ConfigurationTests {
    private static Logger logger = LoggerFactory.getLogger(ConfigurationTests.class);

    @Test
    public void parse() throws Exception{
        String jsonString = Util.loadResource("receiver/mongodb_config_1.json");

        JsonObject jsonObject = JsonUtil.validateJson(jsonString).getAsJsonObject();

        String pipeId = jsonObject.get("pipeId").getAsString();
        JsonArray jsonArray = jsonObject.getAsJsonArray("configuration");

        logger.info("****************************************");
        logger.info("PipeId: "+pipeId);
        for(int i=0; i<jsonArray.size(); i++) {
            JsonObject configurationJson = jsonArray.get(i).getAsJsonObject();
            JsonObject mongoDbConfigurationJson = configurationJson.getAsJsonObject("mongodb_config");
            Configuration configuration = MongoDBConfiguration.parse(mongoDbConfigurationJson.toString());
            JsonUtil.printStdOut(JsonUtil.validateJson(configuration.toString()));
        }
    }
}
