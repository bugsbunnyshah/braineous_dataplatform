package com.appgallabs.dataplatform.receiver.framework;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void receiveData(String pipeId, String data) {
        Registry registry = Registry.getInstance();

        //find the registered store drivers for this pipe
        List<StoreDriver> storeDrivers = registry.findStoreDrivers(pipeId);
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
