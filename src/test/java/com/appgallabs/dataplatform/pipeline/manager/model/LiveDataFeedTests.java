package com.appgallabs.dataplatform.pipeline.manager.model;


import com.appgallabs.dataplatform.pipeline.manager.service.MonitoringContext;
import com.appgallabs.dataplatform.pipeline.manager.service.MonitoringSession;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class LiveDataFeedTests {

    //TODO: activate_me (1.0.0-CR2)
    /*@Test
    public void testReadSnapShot() throws Exception{
        String clientIp = UUID.randomUUID().toString();
        String snapshotId = UUID.randomUUID().toString();
        String pipeId = UUID.randomUUID().toString();

        MonitoringSession monitoringSession = new MonitoringSession();
        monitoringSession.setClientIp(clientIp);
        monitoringSession.setSnapShotId(snapshotId);

        MonitoringContext monitoringContext = new MonitoringContext();
        monitoringContext.setSession(monitoringSession);
        monitoringContext.setPipeId(pipeId);

        LiveDataFeed liveDataFeed = new LiveDataFeed();
        List<String> liveFeedSnapshot = liveDataFeed.readSnapShot(monitoringContext);
        System.out.println(liveFeedSnapshot);
    }*/
}
