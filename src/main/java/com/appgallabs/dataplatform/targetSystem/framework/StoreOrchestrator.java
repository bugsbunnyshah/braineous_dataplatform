package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.Debug;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;

import java.util.List;

public class StoreOrchestrator {

    private static StoreOrchestrator singleton = new StoreOrchestrator();


    private StoreOrchestrator(){
    }

    public static StoreOrchestrator getInstance(){
        //safe-check, cause why not
        if(StoreOrchestrator.singleton == null){
            StoreOrchestrator.singleton = new StoreOrchestrator();
        }
        return StoreOrchestrator.singleton;
    }

    public void receiveData(SecurityToken securityToken, String pipeId, String data) {
        String tenant = securityToken.getPrincipal();

        Debug.out("******STORE_ORCHESTRATOR********");
        Debug.out("PipeId: "+pipeId);
        Debug.out("Data: "+data);
        Debug.out("*******************************&");

        Registry registry = Registry.getInstance();

        //find the registered store drivers for this pipe
        List<StoreDriver> storeDrivers = registry.findStoreDrivers(tenant, pipeId);
        if(storeDrivers == null || storeDrivers.isEmpty()){
            return;
        }


        //TODO: make this transactional (GA)
        //fan out storage to each store
        storeDrivers.stream().forEach(storeDriver -> {
                JsonArray preStorageDataSet = JsonUtil.validateJson(data).getAsJsonArray();

                //TODO: adjust based on configured jsonpath expression (CR1)

                storeDriver.storeData(preStorageDataSet);
            }
        );
    }
}
