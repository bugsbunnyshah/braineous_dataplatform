package com.appgallabs.dataplatform.receiver.framework;

import com.google.gson.JsonArray;

public interface StoreDriver {

    public void storeData(JsonArray dataSet);
}
