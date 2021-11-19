package com.appgallabs.dataplatform.ingestion.endpoint;

import com.appgallabs.dataplatform.ingestion.service.DataFetchAgent;
import com.appgallabs.dataplatform.ingestion.service.DataPushAgent;
import com.appgallabs.dataplatform.ingestion.service.FetchException;
import com.appgallabs.dataplatform.ingestion.service.IngestionService;
import com.appgallabs.dataplatform.ingestion.service.IngestionService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

@Path("dataIngester")
public class DataIngester {
    private static Logger logger = LoggerFactory.getLogger(DataIngester.class);

    @Inject
    private IngestionService ingestionService;

    @Path("fetch")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetch(@RequestBody String input){
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String agentId = jsonObject.get("agentId").getAsString();
            String entity = jsonObject.get("entity").getAsString();

            FlightAgent flightAgent = new FlightAgent();
            this.ingestionService.ingestData(agentId,entity,flightAgent);


            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success",true);
            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }


    //TODO
    private static class FlightAgent implements DataFetchAgent, DataPushAgent {

        @Override
        public JsonArray fetchData() throws FetchException {
            try {
                String responseJson = IOUtils.resourceToString("aviation/flights0.json", StandardCharsets.UTF_8,
                        Thread.currentThread().getContextClassLoader());
                JsonArray jsonArray = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonArray("data");

                System.out.println("************FETCHING_DATA************");

                return jsonArray;
            }
            catch(Exception e){
                throw new FetchException(e);
            }
        }

        @Override
        public void receiveData(JsonArray json) throws FetchException {
            System.out.println("************PUSH_RECEIVED************");
        }
    }
}
