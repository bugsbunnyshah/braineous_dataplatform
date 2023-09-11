package com.appgallabs.dataplatform.ingestion.endpoint;

import com.appgallabs.dataplatform.infrastructure.kafka.EventConsumer;
import com.appgallabs.dataplatform.infrastructure.kafka.EventProcessor;
import com.appgallabs.dataplatform.ingestion.service.IngestionService;
import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.ingestion.util.CSVDataUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("dataMapper")
public class DataMapper {
    private static Logger logger = LoggerFactory.getLogger(DataMapper.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private IngestionService ingestionService;

    private CSVDataUtil csvDataUtil = new CSVDataUtil();

    @Inject
    private EventProcessor eventProcessor;

    @Inject
    private EventConsumer eventConsumer;

    @PostConstruct
    public void start(){
        JsonObject response = this.eventConsumer.checkStatus();
        logger.info(response.toString());
    }

    @Path("map")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response map(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String sourceData = jsonObject.get("sourceData").getAsString();
            String entity = jsonObject.get("entity").getAsString();

            JsonArray array;
            JsonElement sourceIngestion = JsonParser.parseString(sourceData);
            if(sourceIngestion.isJsonObject()){
                array = new JsonArray();
                array.add(sourceIngestion.getAsJsonObject());
            }else{
                array = sourceIngestion.getAsJsonArray();
            }

            JsonObject responseJson = this.eventProcessor.processEvent(array);
            responseJson.addProperty("message", "DATA_SUCCESSFULLY_INGESTED");


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

    @Path("mapXml")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapXmlSourceData(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String xml = jsonObject.get("sourceData").getAsString();
            String entity = jsonObject.get("entity").getAsString();

            JSONObject sourceJson = XML.toJSONObject(xml);
            String sourceData = sourceJson.toString(4);

            JsonArray array;
            JsonElement sourceIngestion = JsonParser.parseString(sourceData);
            if(sourceIngestion.isJsonObject()){
                array = new JsonArray();
                array.add(sourceIngestion.getAsJsonObject());
            }else{
                array = sourceIngestion.getAsJsonArray();
            }

            JsonObject responseJson = this.eventProcessor.processEvent(array);
            responseJson.addProperty("message", "DATA_SUCCESSFULLY_INGESTED");

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

    @Path("mapCsv")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapCsvSourceData(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();
            String sourceData = jsonObject.get("sourceData").getAsString();
            boolean hasHeader = jsonObject.get("hasHeader").getAsBoolean();
            String entity = jsonObject.get("entity").getAsString();

            String[] lines = sourceData.split("\n");
            String[] columns = null;
            int head = 0;
            if(hasHeader) {
                head = 1;
                String header = lines[0];
                columns = header.split(",");
            }
            else
            {
                String top = lines[0];
                int columnCount = top.split(",").length;
                columns = new String[columnCount];
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = "col" + (i+1);
                }
            }
            JsonArray array = new JsonArray();
            int length = lines.length;


            for(int i=head; i<length; i++)
            {
                String line = lines[i];
                String[] data = line.split(",");
                JsonObject row = new JsonObject();
                for(int j=0; j<data.length; j++)
                {
                    row.addProperty(columns[j],data[j]);
                }
                array.add(row);
            }


            JsonObject responseJson = this.mapperService.map(entity,array);

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

    @Path("readDataLakeObject")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readDataLakeObject(@QueryParam("dataLakeId") String dataLakeId)
    {
        try {
            JsonArray storedJson = this.ingestionService.readDataLakeData(dataLakeId);

            Response response = null;
            if(storedJson != null) {
                JsonArray data = new JsonArray();

                for(int i=0; i<storedJson.size();i++){
                    JsonObject json = storedJson.get(i).getAsJsonObject();
                    String jsonString = json.get("data").getAsString();
                    data.add(JsonParser.parseString(jsonString).getAsJsonObject());
                }

                response = Response.ok(data.toString()).build();
            }
            else
            {
                JsonObject error = new JsonObject();
                error.addProperty("dataLakeId", dataLakeId+": NOT_FOUND");
                response = Response.status(404).entity(error.toString()).build();
            }
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
}