package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;

public class MonitoringContext {

    private Pipe pipe;
    private MonitoringSession session;


    public Pipe getPipe() {
        return pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    public MonitoringSession getSession() {
        return session;
    }

    public void setSession(MonitoringSession session) {
        this.session = session;
    }
}
