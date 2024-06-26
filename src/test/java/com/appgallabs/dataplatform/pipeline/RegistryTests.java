package com.appgallabs.dataplatform.pipeline;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.BaseTest;
import test.components.Util;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class RegistryTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(RegistryTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void registerPipe() throws Exception{
        Tenant tenant = this.securityTokenContainer.getTenant();
        String principal = tenant.getPrincipal();

        //register pipe
        String jsonString = Util.loadResource("pipeline/mongodb_config_1.json");
        String inputPipeId = JsonUtil.validateJson(jsonString).getAsJsonObject().get("pipeId").getAsString();

        Registry registry = Registry.getInstance();
        String pipeId = registry.registerPipe(tenant,JsonUtil.validateJson(jsonString).getAsJsonObject());

        List<StagingStore> stagingStores = registry.findStagingStores(principal, pipeId);
        JsonUtil.printStdOut(JsonUtil.validateJson(stagingStores.toString()));

        //asserts
        assertEquals(inputPipeId, pipeId);
        assertFalse(stagingStores.isEmpty());
        assertEquals(1, stagingStores.size());

        //update pipe
        jsonString = Util.loadResource("pipeline/mongodb_config_2.json");
        inputPipeId = JsonUtil.validateJson(jsonString).getAsJsonObject().get("pipeId").getAsString();

        pipeId = registry.registerPipe(tenant,JsonUtil.validateJson(jsonString).getAsJsonObject());

        stagingStores = registry.findStagingStores(principal, pipeId);
        JsonUtil.printStdOut(JsonUtil.validateJson(stagingStores.toString()));

        //asserts
        assertEquals(inputPipeId, pipeId);
        assertFalse(stagingStores.isEmpty());
        assertEquals(2, stagingStores.size());
    }

    @Test
    public void validatePipe() throws Exception{
        Tenant tenant = this.securityTokenContainer.getTenant();
        String principal = tenant.getPrincipal();

        //register pipe
        String jsonString = Util.loadResource("pipeline/invalid_pipeid_config.json");
        String inputPipeId = JsonUtil.validateJson(jsonString).getAsJsonObject().get("pipeId").getAsString();

        Registry registry = Registry.getInstance();

        boolean isValid = true;
        try {
            registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            isValid = false;
        }

        //asserts
        assertFalse(isValid);
    }

    @Test
    public void findDriverConfiguration() throws Exception{
        String jsonString = Util.loadResource("pipeline/mongodb_config_1.json");
        String inputPipeId = JsonUtil.validateJson(jsonString).getAsJsonObject().get("pipeId").getAsString();

        Tenant tenant = this.securityTokenContainer.getTenant();

        Registry registry = Registry.getInstance();
        String pipeId = registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());



        JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();

        //asserts
        assertNotNull(datalakeDriverConfiguration);
    }
}
