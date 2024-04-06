package com.appgallabs.dataplatform.targetSystem.framework.staging;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RecordGenerator {
    private static Logger logger = LoggerFactory.getLogger(RecordGenerator.class);

    public List<Record> parsePayload(Tenant tenant,
                                     String pipeId,
                                     long offset,
                                     String entity,
                                     String payload) throws Exception{
        JsonElement jsonElement = JsonParser.parseString(payload);
        List<Record> input = new ArrayList<>();
        if(jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject inputJson = jsonArray.get(i).getAsJsonObject();
                Record record = this.generateRecord(tenant,
                        pipeId,
                        offset,
                        entity,
                        inputJson);
                input.add(record);
            }
        }else if(jsonElement.isJsonObject()){
            Record record = this.generateRecord(tenant,
                    pipeId,
                    offset,
                    entity,
                    jsonElement.getAsJsonObject());
            input.add(record);
        }
        return input;
    }

    private Record generateRecord(Tenant tenant,
                                  String pipeId,
                                  long offset,
                                  String entity,
                                  JsonObject jsonObject) throws Exception{

        JsonObject metadata = new JsonObject();

        //objectHash
        String objectHash = JsonUtil.getJsonHash(jsonObject);
        metadata.addProperty("objectHash", objectHash);


        //for timestamp
        OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
        Long timestamp = ingestionTime.toEpochSecond();
        metadata.addProperty("timestamp", timestamp);

        //tenant
        String tenantString = tenant.getPrincipal();
        metadata.addProperty("tenant", tenantString);


        //Adddress the pipe
        metadata.addProperty("pipeId", pipeId);

        //entity
        metadata.addProperty("entity", entity);

        metadata.addProperty("kafka_offset", offset);

        RecordMetaData recordMetaData = new RecordMetaData();
        recordMetaData.setMetadata(metadata);
        Record record = new Record();
        record.setRecordMetaData(recordMetaData);
        record.setData(jsonObject);
        return record;
    }
}
