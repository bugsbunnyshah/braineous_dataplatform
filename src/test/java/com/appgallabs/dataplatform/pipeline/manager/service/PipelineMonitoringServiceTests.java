package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.components.BaseTest;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class PipelineMonitoringServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(PipelineMonitoringServiceTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private PipelineMonitoringService pipelineMonitoringService;

    @Test
    public void ingestionProcessMonitoring() throws Exception{
        this.testPipelineMonitoring(PipelineServiceType.INGESTION);
    }

    @Test
    public void dataLakeProcessMonitoring() throws Exception{
        this.testPipelineMonitoring(PipelineServiceType.DATALAKE);
    }

    private void testPipelineMonitoring(PipelineServiceType pipelineServiceType) throws Exception{
        Registry registry = Registry.getInstance();
        String configLocation = "pipeline/manager/service/pipe_config.json";
        String json = Util.loadResource(configLocation);
        JsonObject configJson = JsonUtil.validateJson(json).getAsJsonObject();

        String datasetLocation = "pipeline/manager/service/data.json";
        String jsonString = Util.loadResource(datasetLocation);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        Tenant tenant = new Tenant(securityToken.getPrincipal());
        registry.registerPipe(tenant, configJson);
        String pipeId = configJson.get("pipeId").getAsString();
        String entity = configJson.get("entity").getAsString();
        List<StagingStore> registeredStores = registry.findStagingStores(securityToken.getPrincipal(),
                pipeId);
        StagingStore stagingStore = registeredStores.get(0);

        JsonObject preProcessResult = this.pipelineMonitoringService.preProcess(
                pipelineServiceType,
                configJson,
                securityToken,
                pipeId,
                entity,
                jsonString
        );

        //Assertions
        JsonUtil.printStdOut(preProcessResult);

        //post_process
        JsonObject postProcessResult = this.pipelineMonitoringService.postProcess(
                pipelineServiceType,
                configJson,
                securityToken,
                pipeId,
                entity,
                jsonString
        );

        //Assertions
        JsonUtil.printStdOut(postProcessResult);
    }
}
