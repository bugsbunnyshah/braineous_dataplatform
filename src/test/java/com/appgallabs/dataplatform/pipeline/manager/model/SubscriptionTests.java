package com.appgallabs.dataplatform.pipeline.manager.model;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SubscriptionTests
{
    private static Logger logger = LoggerFactory.getLogger(SubscriptionTests.class);

    @Test
    public void testToJson() throws Exception
    {
        SubscriberGroup group = new SubscriberGroup();
        group.addSubscriber(new Subscriber("1@1.com"));
        group.addSubscriber(new Subscriber("2@1.com"));

        Pipe pushPipe = new Pipe(UUID.randomUUID().toString(),"pipe1");

        Subscription subscription = new Subscription(UUID.randomUUID().toString(),group,pushPipe);

        JsonObject json = subscription.toJson();

        JsonUtil.printStdOut(json);

        logger.info("***********************************");
        
        Subscription deser = Subscription.parse(json.toString());
        JsonUtil.printStdOut(deser.toJson());

        String originalHash = JsonUtil.getJsonHash(subscription.toJson());
        String deserHash = JsonUtil.getJsonHash(deser.toJson());

        assertEquals(originalHash,deserHash);
    }
}
