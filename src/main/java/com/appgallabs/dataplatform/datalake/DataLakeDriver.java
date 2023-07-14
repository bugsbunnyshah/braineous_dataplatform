package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonArray;

import java.util.Map;

public interface DataLakeDriver {
    public String storeIngestion(Tenant tenant, Map<String,Object> flatJson);
    public JsonArray readIngestion(Tenant tenant, String dataLakeId);
}
