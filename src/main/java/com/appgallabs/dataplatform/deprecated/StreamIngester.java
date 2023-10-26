package com.appgallabs.dataplatform.deprecated;

import com.appgallabs.dataplatform.history.service.DataReplayService;
import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.service.MapperService;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.appgallabs.dataplatform.query.ObjectGraphQueryService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class StreamIngester implements Serializable{
    private static Logger logger = LoggerFactory.getLogger(StreamIngester.class);

    public void start(){

    }

    public void stop(){

    }

    public JsonObject submit(Tenant tenant, String environment, SecurityTokenContainer securityTokenContainer,
                             MongoDBJsonStore mongoDBJsonStore,
                             DataReplayService dataReplayService,
                             ObjectGraphQueryService queryService,
                             IngestionService ingestionService,
                             String entity,
                             JsonArray sourceData)
    {
        JsonObject json = new JsonObject();

        if(securityTokenContainer != null) {
            if (StreamIngesterContext.getStreamIngesterContext() != null) {
                StreamIngesterContext streamIngesterContext = StreamIngesterContext.getStreamIngesterContext();
                streamIngesterContext.setEnvironment(environment);
                streamIngesterContext.setSecurityTokenContainer(securityTokenContainer);
                streamIngesterContext.setDataReplayService(dataReplayService);
                streamIngesterContext.setMongoDBJsonStore(mongoDBJsonStore);
                streamIngesterContext.setQueryService(queryService);
                streamIngesterContext.setIngestionService(ingestionService);
            }

            String dataLakeId = UUID.randomUUID().toString();;
            String chainId = "/" + tenant.getPrincipal() + "/" + dataLakeId;

            StreamObject streamObject = new StreamObject();
            streamObject.setEntity(entity);
            streamObject.setDataLakeId(dataLakeId);
            streamObject.setChainId(chainId);
            streamObject.setData(sourceData.toString());
            streamObject.setPrincipal(tenant.getPrincipal());
            streamObject.setBatchSize(sourceData.size());
            StreamIngesterContext.getStreamIngesterContext().addStreamObject(streamObject);

            json.addProperty("entity",entity);
            json.addProperty("dataLakeId", dataLakeId);
            json.addProperty("chainId",chainId);
            json.addProperty("tenant",tenant.getPrincipal());
            json.addProperty("batchSize",sourceData.size());

            return json;
        }
        else {
            return new JsonObject();
        }
    }

    private void startIngestion(String s)
    {
        try {
            System.out.println(s);


            JsonObject streamObject = JsonParser.parseString(s).getAsJsonObject();
            String dataLakeId = streamObject.get("dataLakeId").getAsString();
            String principal = streamObject.get("tenant").getAsString();
            String chainId = streamObject.get("chainId").getAsString();
            String data = streamObject.get("data").getAsString();
            String entity = "not_specified";
            if(streamObject.has("entity") && !streamObject.get("entity").isJsonNull()) {
                entity = streamObject.get("entity").getAsString();
            }
            int batchSize = streamObject.get("batchSize").getAsInt();

            JsonElement root = JsonParser.parseString(data);
            //JsonUtil.print(root);

            if (root.isJsonPrimitive()) {
                return;
            }

            HierarchicalSchemaInfo sourceSchemaInfo = MapperService.populateHierarchialSchema(root.toString(),
                    root.toString(), null);

            HierarchicalSchemaInfo destinationSchemaInfo = MapperService.populateHierarchialSchema(root.toString(),
                    root.toString(), null);


            FilteredSchemaInfo f1 = new FilteredSchemaInfo(sourceSchemaInfo);
            f1.addElements(sourceSchemaInfo.getElements(Entity.class));
            FilteredSchemaInfo f2 = new FilteredSchemaInfo(destinationSchemaInfo);
            f2.addElements(destinationSchemaInfo.getElements(Entity.class));
            Map<SchemaElement, Double> scores = MapperService.findMatches(f1, f2, sourceSchemaInfo.getElements(Entity.class));
            logger.info("*************************************");
            logger.info(scores.toString());
            logger.info("*************************************");

            JsonObject local = MapperService.performMapping(scores, root.toString());
            StreamIngesterContext.getStreamIngesterContext().
                    ingestData(principal,entity, dataLakeId, chainId,batchSize,local);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }
}
