package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonObject;

import java.util.List;

public interface Storage {
    /**
     * This used to configure your Store Driver.
     *
     * Configuration is specified as a json object.
     *
     * Here is a sample configuration
     *
     *     {
     *       "storeDriver" : "com.appgallabs.dataplatform.targetSystem.core.driver.MySqlStoreDriver",
     *       "name": "scenario1_store_mysql",
     *       "config": {
     *         "connectionString": "jdbc:mysql://localhost:3306/braineous_staging_database",
     *         "username": "root",
     *         "password": ""
     *       },
     *       "jsonpathExpression": "jsonpath:1"
     *     }
     *
     * @param configJson
     */
    public void configure(JsonObject configJson);

    public String getName();

    public JsonObject getConfiguration();

    public void storeData(Tenant tenant, String pipeId,
            String entity, List<Record> dataset);

    public List<Record> getRecords(Tenant tenant, String pipeId, String entity);
}
