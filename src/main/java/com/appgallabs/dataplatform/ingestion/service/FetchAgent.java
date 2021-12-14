package com.appgallabs.dataplatform.ingestion.service;

public interface FetchAgent {
    public void startFetch();
    public boolean isStarted();
    public void setEntity(String entity);
}
