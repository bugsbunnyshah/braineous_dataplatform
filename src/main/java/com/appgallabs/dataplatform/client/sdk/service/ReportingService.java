package com.appgallabs.dataplatform.client.sdk.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReportingService {
    private static ReportingService singleton = new ReportingService();

    private ReportingService(){

    }

    public static ReportingService getInstance(){
        //safe-check, cause why not
        if(ReportingService.singleton == null){
            ReportingService.singleton = new ReportingService();
        }
        return ReportingService.singleton;
    }

    public void reportDataError(JsonElement jsonElement){
        //TODO: IMPLEMENT_ME (NOW)
    }
}
