package com.appgallabs.dataplatform.client.sdk.api;

import com.google.gson.JsonObject;

public class DataPlatformService {
    private static DataPlatformService singleton = new DataPlatformService();

    private Configuration configuration;

    private DataPlatformService(){
    }

    public static DataPlatformService getInstance(){
        //safe-check, cause why not
        if(DataPlatformService.singleton == null){
            DataPlatformService.singleton = new DataPlatformService();
        }
        return DataPlatformService.singleton;
    }

    public void configure(Configuration configuration){
        this.configuration = configuration;
    }

    public void registerPipe(JsonObject pipeConf) throws Exception {
        //register/or connect an existing pipeline
        DataPipeline.registerPipe(this.configuration, pipeConf.toString());
    }

    public void sendData(String pipeId, String entity, String payload){
        DataPipeline.sendData(this.configuration, pipeId, entity, payload);
    }

    public void print(String pipeId, String entity, String selectSql){
        DataPipeline.print(this.configuration, pipeId, entity, selectSql);
    }
}
