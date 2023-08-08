package com.appgallabs.dataplatform.ingestion.pipeline;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class DataLakeSinkFunction implements SinkFunction<DataEvent> {

    @Override
    public void invoke(DataEvent value, Context context) throws Exception {
        SinkFunction.super.invoke(value, context);
        System.out.println(value + ">" + value.isProcessed());
    }
}
