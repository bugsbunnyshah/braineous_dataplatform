package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.inject.spi.CDI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class DataLakeSinkFunction implements SinkFunction<DataEvent> {

    private SecurityToken securityToken;

    public DataLakeSinkFunction(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }

    @Override
    public void invoke(DataEvent value, Context context) throws Exception {
        SinkFunction.super.invoke(value, context);

        Config config = ConfigProvider.getConfig();

        //TODO: for custom datalake
        //this.dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        //this.dataLakeDriver = dataLakeDriverInstance.
        //        select(NamedLiteral.of(dataLakeDriverName)).get();

        //for core datalake (MongoDB)
        DataLakeDriver dataLakeDriver = CDI.current().select(DataLakeDriver.class).get();

        //for timestamp
        OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
        Long timestamp = ingestionTime.toEpochSecond();

        //objectHash
        String objectHash = value.toString();

        //tenant
        Tenant tenant = new Tenant(this.securityToken.getPrincipal());
        String tenantString = tenant.toString();

        //entity
        String entity = value.getEntity();


        //store into datalake
        Map<String,Object> fieldMap = new HashMap<>();
        fieldMap.put("tenant",tenantString);
        fieldMap.put("objectHash",objectHash);
        fieldMap.put("timestamp",timestamp);
        fieldMap.put("entity",entity);
        fieldMap.put(value.getFieldName(),value.getFieldValue());
        String datalakeId = dataLakeDriver.storeIngestion(tenant, fieldMap);

        /*System.out.println(value + ">" + value.isProcessed() + ">" + value.getFieldName() + ">" + value.getFieldValue());
        System.out.println(this.securityToken);
        System.out.println(this.dataLakeDriver);
        System.out.println(timestamp);
        System.out.println(objectHash);
        System.out.println(tenant);
        System.out.println(entity);
        System.out.println(datalakeId);
        System.out.println("**********************************");*/
    }
}
