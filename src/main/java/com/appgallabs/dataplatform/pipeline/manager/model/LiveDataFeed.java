package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.pipeline.manager.service.MonitoringContext;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LiveDataFeed {

    public LiveDataFeed(){

    }

    public List<String> readSnapShot(MonitoringContext monitoringContext){
       List<String> snapshot = new ArrayList<>();
       return snapshot;
    }
}
