package com.appgallabs.dataplatform.targetSystem.framework;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface StoreDriver {

    public void configure(JsonObject configJson);

    public void storeData(JsonArray dataSet);
}
