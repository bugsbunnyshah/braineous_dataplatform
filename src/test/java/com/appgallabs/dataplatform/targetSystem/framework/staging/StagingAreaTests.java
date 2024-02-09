package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import test.components.BaseTest;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class StagingAreaTests extends BaseTest {

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private StagingArea stagingArea;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Tenant tenant = this.securityTokenContainer.getTenant();

        String jsonString = test.components.Util.loadResource("targetSystem/framework/staging/pipeline_config_1.json");

        Registry registry = Registry.getInstance();
        registry.registerPipe(tenant, JsonUtil.validateJson(jsonString).getAsJsonObject());
    }

    @Test
    public void receiveData() throws Exception{
        String datasetLocation = "ingestion/algorithm/input_array.json";
        String json = Util.loadResource(datasetLocation);

        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        Tenant tenant = new Tenant(securityToken.getPrincipal());
        String pipeId = "book_pipe";
        String entity = "books";
        String data = json;

        this.stagingArea.receiveDataForStorage(this.securityTokenContainer.getSecurityToken(),
                pipeId,
                entity,
                data);

        Storage storage = this.stagingArea.runIntegrationAgent(
                this.securityTokenContainer.getSecurityToken(),
                pipeId, entity);

        //assert
        List<Record> stored = storage.getRecords(tenant,
                pipeId,
                entity);
        JsonUtil.printStdOut(JsonUtil.validateJson(stored.toString()));
        assertEquals("DA7294FB7F1648812AE4168F9A4C7402" , stored.get(0).getRecordMetaData()
                .getMetadata()
                .get("objectHash").getAsString());
        assertEquals("AF72AFAC8952ED2F861E500F84EA152F" , stored.get(1).getRecordMetaData()
                .getMetadata()
                .get("objectHash").getAsString());
    }
}
