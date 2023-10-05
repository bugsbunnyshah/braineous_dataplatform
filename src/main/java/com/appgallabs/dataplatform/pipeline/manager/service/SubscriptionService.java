package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SubscriptionService {
    private static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    public Subscription getSubscription(String subscriptionId){
        return null;
    }

    public List<Subscription> getAllSubscriptions(){
        return null;
    }

    public void createSubscription(Subscription subscription){

    }

    public void updateSubscription(Subscription subscription){

    }

    public void deleteSubscription(String subscriptionId){

    }
}
