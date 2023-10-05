package com.appgallabs.dataplatform.pipeline.manager.model;

import java.io.Serializable;

public class Subscription implements Serializable {

    private String subscriptionId;

    private SubscriberGroup group;
    private Pipe pipe;

    public Subscription(String subscriptionId,SubscriberGroup group, Pipe pipe) {
        this.subscriptionId = subscriptionId;
        this.group = group;
        this.pipe = pipe;
    }

    public SubscriberGroup getGroup() {
        return group;
    }

    public void setGroup(SubscriberGroup group) {
        this.group = group;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Pipe getPipe() {
        return pipe;
    }
}
