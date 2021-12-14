package com.appgallabs.dataplatform.ingestion.service;

public interface FetchAgent {
    void startFetch();
    boolean isStarted();
    void setEntity(String entity);
}
