package com.appgallabs.dataplatform.pipeline.manager.endpoint;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.pipeline.manager.service.PipeService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @Path("live_snapshot")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response liveSnapShot(@RequestBody String input){
        try {
            //Pipe deployedPipe = Pipe.parse(input);
            //Pipe updated = this.pipeService.moveToDeployed(deployedPipe);

            JsonObject responseJson = new JsonObject();

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
