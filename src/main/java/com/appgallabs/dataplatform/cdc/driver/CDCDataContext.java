package com.appgallabs.dataplatform.cdc.driver;

import com.google.gson.JsonObject;

public class CDCDataContext {
    private JsonObject record;

    private String table;

    public JsonObject getRecord() {
        return record;
    }

    public void setRecord(JsonObject record) {
        this.record = record;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
