package com.appgallabs.dataplatform.receiver.framework;

import java.util.HashMap;
import java.util.Map;

public class StoreOrchestrator {

    private static StoreOrchestrator singleton = new StoreOrchestrator();

    Registry registry;

    private StoreOrchestrator(){
        this.registry = new Registry();
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public static StoreOrchestrator getInstance(){
        //safe-check, cause why not
        if(StoreOrchestrator.singleton == null){
            StoreOrchestrator.singleton = new StoreOrchestrator();
        }
        return StoreOrchestrator.singleton;
    }

    public void receiveData(String pipeId, String data) {
        //find the registered store drivers for this pipe

        //TODO: make this transactional (GA)
        //fan out storage to each store
    }
}
