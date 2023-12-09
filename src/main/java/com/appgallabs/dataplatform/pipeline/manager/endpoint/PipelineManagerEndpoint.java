package com.appgallabs.dataplatform.pipeline.manager.endpoint;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.service.PipeNotFoundException;
import com.appgallabs.dataplatform.pipeline.manager.service.PipeService;
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

@Path("pipeline_manager")
public class PipelineManagerEndpoint {
    private static Logger logger = LoggerFactory.getLogger(PipelineManagerEndpoint.class);

    @Inject
    private PipeService pipeService;

    @Path("move_to_development")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveToDevelopment(@RequestBody String input){
        try {
            Pipe developmentPipe = Pipe.parse(input);

            Pipe updated = this.pipeService.moveToDevelopment(developmentPipe);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "PIPE_SUCCESSFULLY_REGISTERED");
            responseJson.add("pipe", updated.toJson());

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

    @Path("move_to_staged")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveToStaged(@RequestBody String input){
        try {
            Pipe stagedPipe = Pipe.parse(input);
            Pipe updated = this.pipeService.moveToStaged(stagedPipe);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "PIPE_SUCCESSFULLY_STAGED");
            responseJson.add("pipe", updated.toJson());

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

    @Path("move_to_deployed")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveToDeployed(@RequestBody String input){
        try {
            Pipe deployedPipe = Pipe.parse(input);
            Pipe updated = this.pipeService.moveToDeployed(deployedPipe);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "PIPE_SUCCESSFULLY_DEPLOYED");
            responseJson.add("pipe", updated.toJson());

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

    @Path("all_pipes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response allPipes(){
        try {
            JsonArray responseJson = this.pipeService.allPipes();

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

    @Path("dev_pipes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response devPipes(){
        try {
            JsonArray responseJson = this.pipeService.devPipes();

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

    @Path("staged_pipes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response stagedPipes(){
        try {
            JsonArray responseJson = this.pipeService.stagedPipes();

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

    @Path("deployed_pipes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployedPipes(){
        try {
            JsonArray responseJson = this.pipeService.deployedPipes();

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

    @Path("live_snapshot")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response liveSnapShot(@RequestBody String input){
        try {
            JsonObject jsonObject = JsonUtil.validateJson(input).getAsJsonObject();
            String clientIp = jsonObject.get("clientIp").getAsString();
            String snapshotId = jsonObject.get("snapshotId").getAsString();
            String pipeName = jsonObject.get("pipeName").getAsString();



            JsonArray responseJson = this.pipeService.getLiveSnapShot(clientIp,
                    snapshotId,
                    pipeName);

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(PipeNotFoundException pne){
            logger.error(pne.getMessage(), pne);
            JsonObject error = new JsonObject();
            error.addProperty("exception", "PIPE_NOT_FOUND");
            return Response.status(404).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("ingestion_stats/{pipeName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ingestion_stats(@PathParam("pipeName") String pipeName){
        try {
            JsonObject responseJson = this.pipeService.getIngestionStats(pipeName);

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(PipeNotFoundException pne){
            logger.error(pne.getMessage(), pne);
            JsonObject error = new JsonObject();
            error.addProperty("exception", "PIPE_NOT_FOUND");
            return Response.status(404).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("delivery_stats/{pipeName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response delivery_stats(@PathParam("pipeName") String pipeName){
        try {
            JsonObject responseJson = this.pipeService.getDeliveryStats(pipeName);

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }
        catch(PipeNotFoundException pne){
            logger.error(pne.getMessage(), pne);
            JsonObject error = new JsonObject();
            error.addProperty("exception", "PIPE_NOT_FOUND");
            return Response.status(404).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}
