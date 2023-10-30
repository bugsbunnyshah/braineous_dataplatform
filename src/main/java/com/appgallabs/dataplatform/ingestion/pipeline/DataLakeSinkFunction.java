package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.inject.spi.CDI;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class DataLakeSinkFunction implements SinkFunction<String> {

    private SecurityToken securityToken;

    private DataLakeDriver dataLakeDriver;

    public DataLakeSinkFunction(SecurityToken securityToken, DataLakeDriver dataLakeDriver) {
        this.securityToken = securityToken;
        this.dataLakeDriver = dataLakeDriver;
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        JsonObject object = JsonUtil.validateJson(value).getAsJsonObject();

        //for timestamp
        OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
        Long timestamp = ingestionTime.toEpochSecond();

        //objectHash
        String objectHash = JsonUtil.getJsonHash(object);

        //tenant
        Tenant tenant = new Tenant(this.securityToken.getPrincipal());
        String tenantString = tenant.toString();

        //TODO: (CR1)
        String entity = TempConstants.ENTITY;

        //store into datalake
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(value);

        //FileUtils.write(new File("flattenJson.debug"), flattenJson.toString(),
        //        StandardCharsets.UTF_8);

        int count = 0;
        Set<Map.Entry<String, Object>> entries = flattenJson.entrySet();
        for(Map.Entry<String, Object> entry: entries){
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue().toString();

            Map<String,Object> fieldMap = new HashMap<>();
            fieldMap.put("tenant",tenantString);
            fieldMap.put("objectHash",objectHash);
            fieldMap.put("timestamp",timestamp);
            fieldMap.put("entity",entity);
            fieldMap.put(fieldName,fieldValue);

            String datalakeId = this.dataLakeDriver.storeIngestion(tenant, fieldMap);

            //FileUtils.write(new File("datalakeId"+count+".debug"), datalakeId,
            //        StandardCharsets.UTF_8);
            //count++;
        }
    }
}
