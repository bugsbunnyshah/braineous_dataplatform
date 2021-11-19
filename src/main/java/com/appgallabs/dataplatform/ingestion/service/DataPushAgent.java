package com.appgallabs.dataplatform.ingestion.service;

import com.google.gson.JsonArray;

public interface DataPushAgent {
    public void receiveData(JsonArray json) throws FetchException;
}
