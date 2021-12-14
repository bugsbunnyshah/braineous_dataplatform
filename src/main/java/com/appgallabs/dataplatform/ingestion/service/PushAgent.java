package com.appgallabs.dataplatform.ingestion.service;

import com.google.gson.JsonArray;

public interface PushAgent {
    public void receiveData(JsonArray data);
    public void setEntity(String entity);
}
