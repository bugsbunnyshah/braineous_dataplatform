package com.appgallabs.dataplatform.client.sdk.api;

import com.appgallabs.dataplatform.client.sdk.infrastructure.StreamingAgent;
import com.appgallabs.dataplatform.client.sdk.network.DataPipelineClient;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.appgallabs.dataplatform.util.Util;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class DataPlatformService {
    private static DataPlatformService singleton = new DataPlatformService();

    //pipeId -> configuration map
    private Map<String, Configuration> pipelines = new HashMap<>();

    private DataPlatformService(){
    }

    public static DataPlatformService getInstance(){
        //safe-check, cause why not
        if(DataPlatformService.singleton == null){
            DataPlatformService.singleton = new DataPlatformService();
        }
        return DataPlatformService.singleton;
    }

    public void registerPipe(String configLocation) throws Exception {
        String principal = "ffb2969c-5182-454f-9a0b-f3f2fb0ebf75";
        String apiKey = principal;
        Tenant tenant = new Tenant(principal);

        //configure the DataPipeline Client
        Configuration configuration = new Configuration().
                ingestionHostUrl("http://localhost:8080/").
                apiKey(principal).
                apiSecret("5960253b-6645-41bf-b520-eede5754196e").
                streamSizeInObjects(0);

        //register/or connect an existing pipeline
        String pipelineConfig = Util.loadResource(configLocation);
        DataPipeline.registerPipe(configuration, pipelineConfig);

        JsonObject configJson = JsonUtil.validateJson(pipelineConfig).getAsJsonObject();
        String pipeId = configJson.get("pipeId").getAsString();
        this.pipelines.put(pipeId, configuration);
    }

    public void sendData(String pipeId, String entity, String payload){
        Configuration configuration = this.pipelines.get(pipeId);

        DataPipeline.sendData(configuration, pipeId, entity, payload);
    }
}
