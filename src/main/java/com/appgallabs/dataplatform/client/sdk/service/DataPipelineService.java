package com.appgallabs.dataplatform.client.sdk.service;

import com.appgallabs.dataplatform.client.sdk.infrastructure.StreamingAgent;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;

public class DataPipelineService {
    private static DataPipelineService singleton = new DataPipelineService();

    private DataPipelineClient dataPipelineClient;

    private DataPipelineService(){
        this.dataPipelineClient = DataPipelineClient.getInstance();
    }

    public static DataPipelineService getInstance(){
        //safe-check, cause why not
        if(DataPipelineService.singleton == null){
            DataPipelineService.singleton = new DataPipelineService();
        }
        return DataPipelineService.singleton;
    }

    public void sendData(String payload){
        StreamingAgent.getInstance().sendData(payload);
    }
}
