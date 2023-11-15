package com.appgallabs.dataplatform.receiver.framework;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface StoreDriver {

    public void configure(JsonObject configJson);

    public void storeData(JsonArray dataSet);
}
