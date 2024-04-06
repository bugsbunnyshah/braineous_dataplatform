package com.appgallabs.dataplatform.pipeline.manager.endpoint;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.model.Subscription;
import com.appgallabs.dataplatform.pipeline.manager.service.PipeService;
import com.appgallabs.dataplatform.pipeline.manager.service.SubscriptionService;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("subscription_manager")
public class SubscriptionManagerEndpoint {
    private static Logger logger = LoggerFactory.getLogger(SubscriptionManagerEndpoint.class);

    @Inject
    private SubscriptionService subscriptionService;

    @Path("subscriptions_all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscriptionsAll(@RequestBody String input){
        try {
            JsonArray responseJson = this.subscriptionService.getAllSubscriptions();

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("get_subscription/{subscriptionId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscription(@PathParam("subscriptionId") String subscriptionId){
        try {
            Subscription subscription = this.subscriptionService.getSubscription(subscriptionId);

            JsonObject responseJson = subscription.toJson();

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("create_subscription")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSubscription(@RequestBody String input){
        try {
            JsonObject inputJson = JsonUtil.validateJson(input).getAsJsonObject();
            Subscription subscription = Subscription.parse(inputJson.toString());

            this.subscriptionService.createSubscription(subscription);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "SUBSCRIPTION_CREATE_SUCCESS");
            subscription = this.subscriptionService.getSubscription(subscription.getSubscriptionId());
            responseJson.add("subscription", subscription.toJson());

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("update_subscription")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSubscription(@RequestBody String input){
        try {
            JsonObject inputJson = JsonUtil.validateJson(input).getAsJsonObject();
            Subscription subscription = Subscription.parse(inputJson.toString());

            this.subscriptionService.updateSubscription(subscription);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "SUBSCRIPTION_UPDATE_SUCCESS");
            subscription = this.subscriptionService.getSubscription(subscription.getSubscriptionId());
            responseJson.add("subscription", subscription.toJson());

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("delete_subscription/{subscriptionId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSubscription(@PathParam("subscriptionId") String subscriptionId){
        try {
            String deletedSubscriptionId = this.subscriptionService.deleteSubscription(subscriptionId);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "SUBSCRIPTION_DELETE_SUCCESS");
            responseJson.addProperty("subscriptionId", deletedSubscriptionId);

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}
