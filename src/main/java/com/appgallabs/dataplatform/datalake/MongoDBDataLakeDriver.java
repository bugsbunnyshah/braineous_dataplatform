package com.appgallabs.dataplatform.datalake;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonArray;

import java.io.Serializable;
import java.util.Map;

public class MongoDBDataLakeDriver implements DataLakeDriver, Serializable {
    @Override
    public String name() {
        return null;
    }

    @Override
    public String storeIngestion(Tenant tenant, Map<String, Object> flatJson) {
        return null;
    }

    @Override
    public JsonArray readIngestion(Tenant tenant, String dataLakeId) {
        return null;
    }
}
