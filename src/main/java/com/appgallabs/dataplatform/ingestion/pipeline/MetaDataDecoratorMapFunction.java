package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.apache.flink.api.common.functions.MapFunction;

public class MetaDataDecoratorMapFunction implements MapFunction<DataEvent, DataEvent> {

    private SecurityToken securityToken;

    public MetaDataDecoratorMapFunction(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }

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
