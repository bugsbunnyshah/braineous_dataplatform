package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.LiveDataFeed;

public class MonitoringSession {
    private String clientIp;
    private String snapShotId;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getSnapShotId() {
        return snapShotId;
    }

    public void setSnapShotId(String snapShotId) {
        this.snapShotId = snapShotId;
    }
}
