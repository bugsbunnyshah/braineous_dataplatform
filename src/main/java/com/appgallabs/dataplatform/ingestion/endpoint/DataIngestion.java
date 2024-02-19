package com.appgallabs.dataplatform.ingestion.endpoint;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.infrastructure.kafka.EventConsumer;
import com.appgallabs.dataplatform.infrastructure.kafka.EventProducer;
import com.appgallabs.dataplatform.infrastructure.kafka.KafkaSession;
import com.appgallabs.dataplatform.ingestion.util.CSVDataUtil;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.util.JsonUtil;

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


@Path("ingestion")
public class DataIngestion {
    private static Logger logger = LoggerFactory.getLogger(DataIngestion.class);

    private CSVDataUtil csvDataUtil = new CSVDataUtil();

    @Inject
    private KafkaSession kafkaSession;

    @Inject
    private EventConsumer eventConsumer;

    @Inject
    private EventProducer eventProducer;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @PostConstruct
    public void start(){
        try {
            JsonObject response = this.eventConsumer.checkStatus();
            logger.info(response.toString());

            this.eventProducer.start();
            this.eventConsumer.start();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Path("register_pipe")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPipe(@RequestBody String input)
    {
        try
        {
            Tenant tenant = this.securityTokenContainer.getTenant();

            JsonObject responseJson = new JsonObject();

            Registry registry = Registry.getInstance();
            JsonObject pipeRegistration = JsonUtil.validateJson(input).getAsJsonObject();
            String pipeId = pipeRegistration.get("pipeId").getAsString();

            //registry
            registry.registerPipe(tenant,pipeRegistration);

            //kafka register
            this.kafkaSession.registerPipe(pipeId);

            responseJson.addProperty("pipeId",pipeId);
            responseJson.addProperty("message", "PIPE_SUCCESSFULLY_REGISTERED");

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

    @Path("json")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response json(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String sourceData = jsonObject.get("sourceData").getAsString();
            String entity = jsonObject.get("entity").getAsString();
            String pipeId = jsonObject.get("pipeId").getAsString();

            JsonArray array;
            JsonElement sourceIngestion = JsonParser.parseString(sourceData);

            if(sourceIngestion.isJsonObject()){
                array = new JsonArray();
                array.add(sourceIngestion.getAsJsonObject());
            }else{
                array = sourceIngestion.getAsJsonArray();
            }

            JsonObject responseJson = this.eventProducer.processEvent(
                    pipeId,
                    entity,
                    array
            );
            responseJson.addProperty("message", "DATA_SUCCESSFULLY_INGESTED");

            //Get Source Object Hashes
            JsonArray sourceObjectHashes = new JsonArray();
            for(int i=0; i<array.size();i++){
                JsonObject sourceIngestedJson = array.get(i).getAsJsonObject();
                String sourceObjectHash = JsonUtil.getJsonHash(sourceIngestedJson);
                sourceObjectHashes.add(sourceObjectHash);
            }
            responseJson.add("data_lake_ids", sourceObjectHashes);

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

    @Path("xml")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapXmlSourceData(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String xml = jsonObject.get("sourceData").getAsString();
            String entity = jsonObject.get("entity").getAsString();
            String pipeId = jsonObject.get("pipeId").getAsString();

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

            JsonObject responseJson = this.eventProducer.processEvent(
                    pipeId,
                    entity,
                    array
            );
            responseJson.addProperty("message", "DATA_SUCCESSFULLY_INGESTED");

            //Get Source Object Hashes
            JsonArray sourceObjectHashes = new JsonArray();
            for(int i=0; i<array.size();i++){
                JsonObject sourceIngestedJson = array.get(i).getAsJsonObject();
                String sourceObjectHash = JsonUtil.getJsonHash(sourceIngestedJson);
                sourceObjectHashes.add(sourceObjectHash);
            }
            responseJson.add("data_lake_ids", sourceObjectHashes);

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

    @Path("csv")
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
            String pipeId = jsonObject.get("pipeId").getAsString();

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


            JsonObject responseJson = this.eventProducer.processEvent(
                    pipeId,
                    entity,
                    array
            );
            responseJson.addProperty("message", "DATA_SUCCESSFULLY_INGESTED");

            //Get Source Object Hashes
            JsonArray sourceObjectHashes = new JsonArray();
            for(int i=0; i<array.size();i++){
                JsonObject sourceIngestedJson = array.get(i).getAsJsonObject();
                String sourceObjectHash = JsonUtil.getJsonHash(sourceIngestedJson);
                sourceObjectHashes.add(sourceObjectHash);
            }
            responseJson.add("data_lake_ids", sourceObjectHashes);

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
}