package com.appgallabs.dataplatform.pipeline;

import com.appgallabs.dataplatform.receiver.framework.StoreDriver;
import com.appgallabs.dataplatform.util.JsonUtil;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import test.components.Util;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class RegistryTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(RegistryTests.class);

    @Test
    public void registerPipe() throws Exception{
        String jsonString = Util.loadResource("receiver/mongodb_config_1.json");
        String inputPipeId = JsonUtil.validateJson(jsonString).getAsJsonObject().get("pipeId").getAsString();

        Registry registry = Registry.getInstance();
        String pipeId = registry.registerPipe(JsonUtil.validateJson(jsonString).getAsJsonObject());

        List<StoreDriver> storeDrivers = registry.findStoreDrivers(pipeId);
        JsonUtil.printStdOut(JsonUtil.validateJson(storeDrivers.toString()));

        //asserts
        assertEquals(inputPipeId, pipeId);
        assertFalse(storeDrivers.isEmpty());
    }

    @Test
    public void registerCorePipeline() throws Exception{
        String jsonString = Util.loadResource("pipeline/mongodb_pipeline.json");
        String inputPipeId = JsonUtil.validateJson(jsonString).getAsJsonObject().get("pipeId").getAsString();

        Registry registry = Registry.getInstance();
        String pipeId = registry.registerPipe(JsonUtil.validateJson(jsonString).getAsJsonObject());

        List<StoreDriver> storeDrivers = registry.findStoreDrivers(pipeId);
        JsonUtil.printStdOut(JsonUtil.validateJson(storeDrivers.toString()));

        //asserts
        assertEquals(inputPipeId, pipeId);
        assertFalse(storeDrivers.isEmpty());
    }
}
