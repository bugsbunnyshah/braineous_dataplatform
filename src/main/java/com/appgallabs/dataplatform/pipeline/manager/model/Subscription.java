package com.appgallabs.dataplatform.pipeline.manager.model;

import java.io.Serializable;

public class Subscription implements Serializable {

    private SubscriberGroup group;

    public Subscription(SubscriberGroup group) {
        this.group = group;
    }

    public SubscriberGroup getGroup() {
        return group;
    }

    public void setGroup(SubscriberGroup group) {
        this.group = group;
    }
}
