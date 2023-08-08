package com.appgallabs.dataplatform.ingestion.pipeline;

import org.apache.flink.api.common.functions.MapFunction;

public class MetaDataDecoratorMapFunction implements MapFunction<DataEvent, SinkEvent> {

    @Override
    public SinkEvent map(DataEvent dataEvent) throws Exception {
        System.out.println("*******DECORATE******");
        SinkEvent nextEvent = new SinkEvent(dataEvent.getJson());
        return nextEvent;
    }
}
