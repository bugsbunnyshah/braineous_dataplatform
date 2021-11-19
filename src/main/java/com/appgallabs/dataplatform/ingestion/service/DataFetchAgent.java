package com.appgallabs.dataplatform.ingestion.service;

import com.google.gson.JsonArray;

public interface DataFetchAgent {
    public JsonArray fetchData() throws FetchException;
}
