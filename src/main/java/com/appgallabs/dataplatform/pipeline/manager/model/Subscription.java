package com.appgallabs.dataplatform.pipeline.manager.model;

import java.io.Serializable;

public class Subscription implements Serializable {

    private SubscriberGroup group;
    private Pipe pipe;

    public Subscription(SubscriberGroup group, Pipe pipe) {
        this.group = group;
        this.pipe = pipe;
    }

    public SubscriberGroup getGroup() {
        return group;
    }

    public void setGroup(SubscriberGroup group) {
        this.group = group;
    }
}
