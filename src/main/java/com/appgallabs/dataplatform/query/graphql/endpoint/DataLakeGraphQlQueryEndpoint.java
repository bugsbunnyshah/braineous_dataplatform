package com.appgallabs.dataplatform.query.graphql.endpoint;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.graphql.service.QueryExecutor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/data/lake/query")
public class DataLakeGraphQlQueryEndpoint
{
    private static Logger logger = LoggerFactory.getLogger(DataLakeGraphQlQueryEndpoint.class);

    @Inject
    private QueryExecutor queryExecutor;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String graphqlQuery = jsonObject.get("graphqlQuery").getAsString();

            JsonObject response = new JsonObject();

            return Response.ok(response.toString()).build();
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