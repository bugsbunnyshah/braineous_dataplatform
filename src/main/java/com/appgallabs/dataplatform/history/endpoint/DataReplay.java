package com.appgallabs.dataplatform.history.endpoint;

import com.appgallabs.dataplatform.history.service.DataReplayService;
import com.appgallabs.dataplatform.ingestion.service.ChainNotFoundException;
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
import java.util.List;

@Path("replay")
public class DataReplay {
    private static Logger logger = LoggerFactory.getLogger(DataReplay.class);

    @Inject
    private DataReplayService dataReplayService;

    @Path("chain")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response chain(@QueryParam("oid") String oid)
    {
        try {
            List<JsonObject> diffChain = this.dataReplayService.replayDiffChain(oid);
            return Response.ok(diffChain.toString()).build();
        }
        catch (ChainNotFoundException cne)
        {
            JsonObject error = new JsonObject();
            error.addProperty("exception", cne.getMessage());
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