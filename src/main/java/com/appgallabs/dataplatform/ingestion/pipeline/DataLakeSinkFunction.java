package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

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
        System.out.println(value + ">" + value.isProcessed());
        System.out.println(this.securityToken);
        System.out.println(this.dataLakeDriver);
        System.out.println("**********************************");
    }
}
