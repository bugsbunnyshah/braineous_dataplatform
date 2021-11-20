package com.appgallabs.dataplatform;

import com.appgallabs.dataplatform.configuration.AIPlatformConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/data/microservice")
public class Microservice
{
    private static Logger logger = LoggerFactory.getLogger(Microservice.class);

    @Inject
    private AIPlatformConfig aiPlatformConfig;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response microservice(@RequestBody String json)
    {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            logger.info(jsonObject.toString());

            jsonObject.addProperty("product", "BRAINEOUS DATA PLATFORM");
            jsonObject.addProperty("oid", UUID.randomUUID().toString());
            jsonObject.addProperty("message", "HELLO_AI");

            return Response.ok(jsonObject.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response microserviceGet()
    {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("product", "BRAINEOUS DATA PLATFORM");
            jsonObject.addProperty("oid", UUID.randomUUID().toString());
            jsonObject.addProperty("message", "HELLO_AI");

            return Response.ok(jsonObject.toString()).build();
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