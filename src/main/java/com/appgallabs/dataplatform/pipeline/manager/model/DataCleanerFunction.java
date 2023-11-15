package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;

public class DataCleanerFunction {

    private String function;

    public DataCleanerFunction() {
    }

    public DataCleanerFunction(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public JsonObject toJson(){
            Gson gson = JsonUtil.getGson();
            JsonElement jsonElement = gson.toJsonTree(this);
            return jsonElement.getAsJsonObject();
        }

        public static DataCleanerFunction parse(String jsonString){
            Gson gson = JsonUtil.getGson();

            DataCleanerFunction parsed = gson.fromJson(jsonString,DataCleanerFunction.class);

            return parsed;
        }

        @Override
        public String toString() {
            return this.toJson().toString();
        }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataCleanerFunction that = (DataCleanerFunction) o;
        return function.equals(that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function);
    }
}
