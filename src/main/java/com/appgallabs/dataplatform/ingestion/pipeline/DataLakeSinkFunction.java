package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.datalake.MongoDBDataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonObject;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class DataLakeSinkFunction implements SinkFunction<String> {

    private SecurityToken securityToken;
    private String driverConfiguration;

    public DataLakeSinkFunction(SecurityToken securityToken,String driverConfiguration) {
        this.securityToken = securityToken;
        this.driverConfiguration = driverConfiguration;
    }

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

        //TODO: (CR1)
        String entity = TempConstants.ENTITY;
        metadata.addProperty("entity", entity);

        datalakeObject.add("metadata", metadata);
        datalakeObject.add("source_data", jsonObject);

        //store into datalake
        driver.storeIngestion(tenant, datalakeObject.toString());
    }
}
