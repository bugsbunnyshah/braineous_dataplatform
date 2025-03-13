package com.appgallabs.dataplatform.cdc.driver;

import com.google.gson.JsonObject;

public interface CDCDriver {

    boolean isInsert(JsonObject storeConfigJson, String[] dataKey, JsonObject record);

    boolean isUpdate(JsonObject storeConfigJson, String[] dataKey, JsonObject record);
}
