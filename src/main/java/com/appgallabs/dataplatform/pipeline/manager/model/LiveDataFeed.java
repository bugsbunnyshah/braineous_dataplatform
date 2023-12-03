package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.pipeline.manager.service.MonitoringContext;
import com.appgallabs.dataplatform.util.JsonUtil;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LiveDataFeed {

    public LiveDataFeed(){

    }

    public List<String> readSnapShot(MonitoringContext monitoringContext){
       List<String> snapshot = new ArrayList<>();

       System.out.println("*****LIVE_DATA_FEED**********");
       System.out.println("ClientIP: "+monitoringContext.getSession().getClientIp());
       System.out.println("SnapShotId: "+monitoringContext.getSession().getSnapShotId());
       JsonUtil.printStdOut(monitoringContext.getPipe().toJson());

       return snapshot;
    }
}
