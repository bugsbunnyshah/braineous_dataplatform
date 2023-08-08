package com.appgallabs.dataplatform.ingestion.pipeline;

import org.apache.flink.api.common.functions.MapFunction;

public class MetaDataDecoratorMapFunction implements MapFunction<DataEvent, DataEvent> {

    @Override
    public DataEvent map(DataEvent dataEvent) throws Exception {
        try {
            dataEvent.setProcessed(true);
            return dataEvent;
        }finally{
            Thread.sleep((long)(Math.random() * 1000));
        }
    }
}
