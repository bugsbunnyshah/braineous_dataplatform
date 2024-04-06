package com.appgallabs.dataplatform.reporting;

import com.google.gson.JsonElement;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IngestionReportingService {
    private static IngestionReportingService singleton = new IngestionReportingService();

    private IngestionReportingService(){

    }

    public void reportDataError(JsonElement jsonElement){

    }
}
