package com.appgallabs.dataplatform.cdc.engine;

import com.appgallabs.dataplatform.cdc.driver.DBDataUtil;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.components.Util;

import javax.inject.Inject;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

//TODO: (integration_phase)
//@QuarkusTest
public class CDCConductorTests {
    private static Logger logger = LoggerFactory.getLogger(CDCConductorTests.class);

    //TODO: (integration_phase)
    //@Inject
    private CDCConductor cdcConductor = CDCConductor.getInstance();

    @Test
    public void orchestrateInsert() throws Exception {
        String table = "cdc_test";

        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        JsonObject cdcConfig = JsonUtil.validateJson(configJsonStr).getAsJsonObject();
        final String[] dataKey = {"name", "email"};

        //get the change dataset
        String objStr = Util.loadResource("cdc/insert_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        for(int i=0; i<objJsonArray.size(); i++){
            Random rand = new Random();
            long randValue = rand.nextLong();

            JsonObject obj = objJsonArray.get(i).getAsJsonObject().getAsJsonObject("addr");
            obj.addProperty("email", randValue +"@email.com");
        }
        JsonUtil.printStdOut(objJsonArray);

        String query = "select count(*) from " + table;
        Map<String,String> result = DBDataUtil.query(cdcConfig, query);
        String beforeCountStr = result.get("count(*)");
        int beforeCount = 0;
        if(beforeCountStr != null){
            beforeCount = Integer.parseInt(beforeCountStr);
        }

        this.cdcConductor.orchestrate(
                cdcConfig,
                objJsonArray
        );

        Thread.sleep(5000);


        //assert the result
        result = DBDataUtil.query(cdcConfig, query);
        String afterCountStr = result.get("count(*)");
        int afterCount = 0;
        if(afterCountStr != null){
            afterCount = Integer.parseInt(afterCountStr);
        }

        System.out.println("***********");
        System.out.println("BeforeCount: "+beforeCount);
        System.out.println("AfterCount: "+afterCount);
        System.out.println("***********");
        assertTrue(afterCount == (beforeCount+2));
    }

    @Test
    public void orchestrateUpdate() throws Exception{
        String table = "cdc_test";

        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        JsonObject cdcConfig = JsonUtil.validateJson(configJsonStr).getAsJsonObject();
        final String[] dataKey = {"name", "email"};

        //get the change dataset
        String objStr = Util.loadResource("cdc/update_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonUtil.printStdOut(objJsonArray);

        String query = "select count(*) from " + table;
        Map<String,String> result = DBDataUtil.query(cdcConfig, query);
        String beforeCountStr = result.get("count(*)");
        int beforeCount = 0;
        if(beforeCountStr != null){
            beforeCount = Integer.parseInt(beforeCountStr);
        }

        this.cdcConductor.orchestrate(
                cdcConfig,
                objJsonArray
        );

        Thread.sleep(5000);


        //assert the result
        result = DBDataUtil.query(cdcConfig, query);
        String afterCountStr = result.get("count(*)");
        int afterCount = 0;
        if(afterCountStr != null){
            afterCount = Integer.parseInt(afterCountStr);
        }

        System.out.println("***********");
        System.out.println("BeforeCount: "+beforeCount);
        System.out.println("AfterCount: "+afterCount);
        System.out.println("***********");
        assertTrue(afterCount == beforeCount);
    }

    @Test
    public void orchestrateBoth() throws Exception{
        String table = "cdc_test";

        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        JsonObject cdcConfig = JsonUtil.validateJson(configJsonStr).getAsJsonObject();
        final String[] dataKey = {"name", "email"};

        //get the change dataset
        String objStr = Util.loadResource("cdc/both_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        Random rand = new Random();
        long randValue = rand.nextLong();
        JsonObject obj = objJsonArray.get(0).getAsJsonObject().getAsJsonObject("addr");
        obj.addProperty("email", randValue +"@email.com");
        JsonUtil.printStdOut(objJsonArray);

        String query = "select count(*) from " + table;
        Map<String,String> result = DBDataUtil.query(cdcConfig, query);
        String beforeCountStr = result.get("count(*)");
        int beforeCount = 0;
        if(beforeCountStr != null){
            beforeCount = Integer.parseInt(beforeCountStr);
        }

        this.cdcConductor.orchestrate(
                cdcConfig,
                objJsonArray
        );

        Thread.sleep(5000);


        //assert the result
        result = DBDataUtil.query(cdcConfig, query);
        String afterCountStr = result.get("count(*)");
        int afterCount = 0;
        if(afterCountStr != null){
            afterCount = Integer.parseInt(afterCountStr);
        }

        System.out.println("***********");
        System.out.println("BeforeCount: "+beforeCount);
        System.out.println("AfterCount: "+afterCount);
        System.out.println("***********");
        assertTrue(afterCount == (beforeCount+1));
    }
}
