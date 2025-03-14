package com.appgallabs.dataplatform.cdc.engine;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.Util;

import javax.inject.Inject;

//TODO: (integration_phase)
//@QuarkusTest
public class CDCConductorTests {
    private static Logger logger = LoggerFactory.getLogger(CDCConductorTests.class);

    //TODO: (integration_phase)
    //@Inject
    private CDCConductor cdcConductor = new CDCConductor();

    @Test
    public void orchestrate() throws Exception {
        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        JsonObject cdcConfig = JsonUtil.validateJson(configJsonStr).getAsJsonObject();
        final String[] dataKey = {"name", "email"};

        //get the change dataset
        String objStr = Util.loadResource("cdc/insert_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonUtil.printStdOut(objJsonArray);

        this.cdcConductor.orchestrate(
                cdcConfig,
                objJsonArray
        );
    }
}
