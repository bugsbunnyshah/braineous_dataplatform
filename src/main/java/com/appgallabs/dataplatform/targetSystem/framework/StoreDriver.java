package com.appgallabs.dataplatform.targetSystem.framework;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;

public interface StoreDriver extends Serializable {

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

    /**
     * Implementation logic for storing the dataset processed by the
     * ingestion engine sent as an array of JsonObjects
     *
     * @param dataSet
     */
    public void storeData(JsonArray dataSet);

    public String getName();

    public JsonObject getConfiguration();
}
