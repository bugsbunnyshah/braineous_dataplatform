package com.appgallabs.dataplatform.query.endpoint;

import com.appgallabs.dataplatform.query.ObjectGraphQueryService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/graph/query")
public class ObjectGraphQueryEndpoint
{
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryEndpoint.class);

    @Inject
    private ObjectGraphQueryService objectGraphQueryService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("criteria")
    public Response queryByCriteria(@RequestBody String requestJson)
    {
        try
        {
            JsonObject json = JsonParser.parseString(requestJson).getAsJsonObject();
            String entity = json.get("entity").getAsString();
            JsonObject criteria = json.get("criteria").getAsJsonObject();
            JsonArray array = this.objectGraphQueryService.queryByCriteria(entity,criteria);
            return Response.ok(array.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("navigate")
    public Response queryByNavigation(@RequestBody String requestJson)
    {
        try
        {
            JsonObject json = JsonParser.parseString(requestJson).getAsJsonObject();
            String entity = json.get("entity").getAsString();
            String relationship = json.get("relationship").getAsString();
            JsonObject criteria = json.get("criteria").getAsJsonObject();
            JsonArray array = this.objectGraphQueryService.navigateByCriteria(entity,relationship,
                    criteria);
            return Response.ok(array.toString()).build();
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