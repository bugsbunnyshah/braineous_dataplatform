package com.appgallabs.dataplatform.ingestion.pipeline;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class DataLakeSinkFunction implements SinkFunction<SinkEvent> {

    @Override
    public void invoke(SinkEvent value, Context context) throws Exception {
        SinkFunction.super.invoke(value, context);
        System.out.println("*******STORE******");
        System.out.println(value);
    }
}
