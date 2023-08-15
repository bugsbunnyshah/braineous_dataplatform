package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DataLakeSinkFunction implements SinkFunction<DataEvent> {

    private SecurityToken securityToken;

    private DataLakeDriver dataLakeDriver;

    public DataLakeSinkFunction(SecurityToken securityToken, DataLakeDriver dataLakeDriver) {
        this.securityToken = securityToken;
        this.dataLakeDriver = dataLakeDriver;
    }

    @Override
    public void invoke(DataEvent value, Context context) throws Exception {
        SinkFunction.super.invoke(value, context);

        //for timestamp
        OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
        Long timestamp = ingestionTime.toEpochSecond();

        //objectHash
        String objectHash = value.toString();

        //tenant
        String tenant = this.securityToken.getPrincipal();

        //entity
        String entity = value.getEntity();

        System.out.println(value + ">" + value.isProcessed() + ">" + value.getFieldName() + ">" + value.getFieldValue());
        System.out.println(this.securityToken);
        System.out.println(this.dataLakeDriver);
        System.out.println(timestamp);
        System.out.println(objectHash);
        System.out.println(tenant);
        System.out.println(entity);
        System.out.println("**********************************");

        //TODO(1): store into datalake
    }
}
