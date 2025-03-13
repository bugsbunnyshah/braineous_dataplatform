package com.appgallabs.dataplatform.cdc.driver;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import test.components.Util;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RDBMSCDCDriverTests {

    @Test
    public void insert() throws Exception{
        CDCDriver cdcDriver = RDBMSCDCDriver.getInstance();

        //get storeConfig
        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        //execute the CDC process/algorithm for each record
        JsonObject dbConfigJson = JsonUtil.validateJson(configJsonStr).getAsJsonObject();

        //prepare CDCDataContext
        String table = "cdc_test";
        String objStr = Util.loadResource("cdc/insert_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonObject record = objJsonArray.get(0).getAsJsonObject();
        CDCDataContext dataContext = new CDCDataContext();
        dataContext.setTable(table);
        dataContext.setRecord(record);

        String query = "select count(*) from " + table;
        Map<String,String> result = DBDataUtil.query(dbConfigJson, query);
        String beforeCountStr = result.get("count(*)");
        int beforeCount = 0;
        if(beforeCountStr != null){
            beforeCount = Integer.parseInt(beforeCountStr);
        }

        //insert the record
        cdcDriver.insert(dbConfigJson, dataContext);

        //assert the result
        result = DBDataUtil.query(dbConfigJson, query);
        String afterCountStr = result.get("count(*)");
        int afterCount = 0;
        if(afterCountStr != null){
            afterCount = Integer.parseInt(afterCountStr);
        }

        System.out.println("***********");
        System.out.println("BeforeCount: "+beforeCount);
        System.out.println("AfterCount: "+afterCount);
        System.out.println("***********");
        assertTrue(afterCount > beforeCount);
    }

    @Test
    public void update() throws Exception {
        CDCDriver cdcDriver = RDBMSCDCDriver.getInstance();

        //get storeConfig
        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        //execute the CDC process/algorithm for each record
        JsonObject dbConfigJson = JsonUtil.validateJson(configJsonStr).getAsJsonObject();

        //prepare CDCDataContext
        String table = "cdc_test";
        String objStr = Util.loadResource("cdc/update_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonObject record = objJsonArray.get(0).getAsJsonObject();
        CDCDataContext dataContext = new CDCDataContext();
        dataContext.setTable(table);
        dataContext.setRecord(record);

        //update the record
        int updateCount = cdcDriver.update(dbConfigJson, dataContext);

        //assert the result
        System.out.println("***********");
        System.out.println("UpdateCount: "+updateCount);
        System.out.println("***********");
        assertTrue(updateCount > 0);
    }
}
