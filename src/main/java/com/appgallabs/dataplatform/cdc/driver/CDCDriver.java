package com.appgallabs.dataplatform.cdc.driver;

import com.google.gson.JsonObject;

public interface CDCDriver {

    void insert(JsonObject storeConfigJson, CDCDataContext cdcDataContext);

    int update(JsonObject storeConfigJson, CDCDataContext cdcDataContext);
}
