package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.DataCleanerFunction;
import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DataCleansingService {

    private static Logger logger = LoggerFactory.getLogger(DataCleansingService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private SubscriptionService subscriptionService;


    public Pipe addCleanerFunction(Pipe pipe, DataCleanerFunction dataCleanerFunction){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);

        subscription.getPipe().addCleanerFunction(dataCleanerFunction);

        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public Pipe removeCleanerFunction(Pipe pipe, DataCleanerFunction dataCleanerFunction){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);

        subscription.getPipe().removeCleanerFunction(dataCleanerFunction);

        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public Pipe removeAllCleanerFunctions(Pipe pipe){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);

        subscription.getPipe().clearCleanerFunctions();

        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }
}
