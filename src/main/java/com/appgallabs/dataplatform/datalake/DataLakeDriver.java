package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Map;

public interface DataLakeDriver extends Serializable {

    public void configure(String configJson);

    public String name();

    public void storeIngestion(Tenant tenant, String jsonObject);
}
