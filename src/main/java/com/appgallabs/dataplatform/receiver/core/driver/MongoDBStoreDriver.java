package com.appgallabs.dataplatform.receiver.core.driver;

import com.appgallabs.dataplatform.receiver.framework.StoreDriver;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBStoreDriver implements StoreDriver {
    private static Logger logger = LoggerFactory.getLogger(MongoDBStoreDriver.class);

    private JsonObject configJson;

    @Override
    public void configure(JsonObject configJson) {
        this.configJson = configJson;
    }

    @Override
    public void storeData(JsonArray dataSet) {
        logger.info("************STORIN_MONGODB_STORE*****************");
        JsonUtil.printStdOut(this.configJson);
        JsonUtil.printStdOut(dataSet);
        logger.info("*************************************************");
    }
}
