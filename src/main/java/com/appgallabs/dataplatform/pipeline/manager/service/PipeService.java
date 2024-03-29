package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.PipeStage;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PipeService {
    private static Logger logger = LoggerFactory.getLogger(PipeService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private SubscriptionService subscriptionService;


    public Pipe moveToDevelopment(Pipe pipe){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
        subscription.getPipe().setPipeStage(PipeStage.DEVELOPMENT);
        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public Pipe moveToStaged(Pipe pipe){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
        subscription.getPipe().setPipeStage(PipeStage.STAGED);
        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }

    public Pipe moveToDeployed(Pipe pipe){
        String subscriptionId = pipe.getSubscriptionId();

        Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);
        subscription.getPipe().setPipeStage(PipeStage.DEPLOYED);
        subscription = this.subscriptionService.updateSubscription(subscription);

        return subscription.getPipe();
    }
}
