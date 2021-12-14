package com.appgallabs.dataplatform.ingestion.service;

import com.google.gson.JsonArray;

public interface PushAgent {
    void receiveData(JsonArray data);
    void setEntity(String entity);
}
