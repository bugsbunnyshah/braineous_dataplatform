package com.appgallabs.dataplatform.receiver.core.driver;

import com.appgallabs.dataplatform.receiver.framework.Configuration;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MongoDBConfiguration extends Configuration {

    private String connectionString;

    private String database;

    private String collection;

    public MongoDBConfiguration() {
        super();
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public static Configuration parse(String jsonString){
        Gson gson = JsonUtil.getGson();

        Configuration parsed = gson.fromJson(jsonString,MongoDBConfiguration.class);

        return parsed;
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonElement jsonElement = gson.toJsonTree(this);
        return jsonElement.getAsJsonObject();
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
