package com.appgallabs.dataplatform.receiver.core.driver;

import com.appgallabs.dataplatform.receiver.framework.StoreDriver;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//TODO: IMPLEMENT_ME (CR1)
public class RESTfulStoreDriver implements StoreDriver {
    @Override
    public void configure(JsonObject configJson) {
        JsonUtil.printStdOut(configJson);
    }

    @Override
    public void storeData(JsonArray dataSet) {

    }
}
