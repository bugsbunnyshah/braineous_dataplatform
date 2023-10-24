package com.appgallabs.dataplatform.query.endpoint;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/data/query")
public class DataLakeGraphSqlQueryEndpoint
{
    private static Logger logger = LoggerFactory.getLogger(DataLakeGraphSqlQueryEndpoint.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response all(@QueryParam("entity") String entity)
    {
        try
        {
            JsonArray json = this.mongoDBJsonStore.readByEntity(
                    securityTokenContainer.getTenant(),
                    entity
            );
            return Response.ok(json.toString()).build();
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