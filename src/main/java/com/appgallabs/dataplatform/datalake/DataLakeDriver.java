package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Map;

public interface DataLakeDriver extends Serializable {

    public void configure(JsonObject configJson);

    public String name();

    public String storeIngestion(Tenant tenant, Map<String,Object> flatJson);
    public JsonArray readIngestion(Tenant tenant, String dataLakeId);
}
