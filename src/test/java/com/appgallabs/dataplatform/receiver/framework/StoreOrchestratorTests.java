package com.appgallabs.dataplatform.receiver.framework;

import com.appgallabs.dataplatform.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import test.components.Util;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class StoreOrchestratorTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(StoreOrchestratorTests.class);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        String jsonString = Util.loadResource("receiver/mongodb_config_1.json");

        Registry registry = Registry.getInstance();
        String pipeId = registry.registerPipe(JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void receiveData() throws Exception{
        String jsonString = Util.loadResource("receiver/dataset.json");

        StoreOrchestrator storeOrchestrator = StoreOrchestrator.getInstance();
        storeOrchestrator.receiveData("123", jsonString);
    }
}
