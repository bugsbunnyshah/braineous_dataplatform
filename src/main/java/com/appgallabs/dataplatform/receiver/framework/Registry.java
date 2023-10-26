package com.appgallabs.dataplatform.receiver.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registry {
    private Map<String, List<Configuration>> registry; //pipeId -> StoreDriver

    public Registry() {
        this.registry = new HashMap<>();
    }

    public Map<String, List<Configuration>> getRegistry() {
        return registry;
    }

    public void setRegistry(Map<String, List<Configuration>> registry) {
        this.registry = registry;
    }

    public List<Configuration> findByPipeId(String pipeId){
        return this.registry.get(pipeId);
    }
}
