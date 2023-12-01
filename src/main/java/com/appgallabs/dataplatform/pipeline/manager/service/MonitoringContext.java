package com.appgallabs.dataplatform.pipeline.manager.service;

public class MonitoringContext {

    private String pipeId;
    private MonitoringSession session;


    public String getPipeId() {
        return pipeId;
    }

    public void setPipeId(String pipeId) {
        this.pipeId = pipeId;
    }

    public MonitoringSession getSession() {
        return session;
    }

    public void setSession(MonitoringSession session) {
        this.session = session;
    }
}
