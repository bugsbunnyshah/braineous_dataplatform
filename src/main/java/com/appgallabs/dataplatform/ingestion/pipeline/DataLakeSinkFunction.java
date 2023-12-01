package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.datalake.MongoDBDataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.ingestion.util.FlinkUtil;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DataLakeSinkFunction implements SinkFunction<String> {

    private SecurityToken securityToken;
    private String driverConfiguration;

    private String pipeId;

    private String entity;

    public DataLakeSinkFunction(SecurityToken securityToken,String driverConfiguration,String pipeId, String entity) {
        this.securityToken = securityToken;
        this.driverConfiguration = driverConfiguration;
        this.pipeId = pipeId;
        this.entity = entity;
    }

    //processes a json object
    @Override
    public void invoke(String value, Context context) throws Exception {
        MongoDBDataLakeDriver driver = new MongoDBDataLakeDriver();
        driver.configure(this.driverConfiguration);

        JsonObject datalakeObject = new JsonObject();

        JsonObject jsonObject = JsonUtil.validateJson(value).getAsJsonObject();

        //objectHash
        String objectHash = JsonUtil.getJsonHash(jsonObject);

        JsonObject metadata = new JsonObject();
        metadata.addProperty("objectHash", objectHash);

        //for timestamp
        OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
        Long timestamp = ingestionTime.toEpochSecond();
        metadata.addProperty("timestamp", timestamp);

        //tenant
        Tenant tenant = new Tenant(this.securityToken.getPrincipal());
        String tenantString = tenant.toString();
        metadata.addProperty("tenant", tenantString);


        //Adddress the pipe
        metadata.addProperty("pipeId", pipeId);

        //entity
        metadata.addProperty("entity", entity);

        datalakeObject.add("metadata", metadata);
        datalakeObject.add("source_data", jsonObject);

        //store into datalake
        driver.storeIngestion(tenant, datalakeObject.toString());

        FlinkUtil.log(value);
    }
}
