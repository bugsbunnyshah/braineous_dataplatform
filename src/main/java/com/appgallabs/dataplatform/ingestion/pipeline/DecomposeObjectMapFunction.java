package com.appgallabs.dataplatform.ingestion.pipeline;

import org.apache.flink.api.common.functions.MapFunction;

public class DecomposeObjectMapFunction implements MapFunction<DataEvent,DataEvent> {
    @Override
    public DataEvent map(DataEvent dataEvent) throws Exception {
        System.out.println("*******DECOMPOSE******");
        DataEvent nextEvent = new DataEvent(dataEvent.getJson());
        return nextEvent;
    }
}
