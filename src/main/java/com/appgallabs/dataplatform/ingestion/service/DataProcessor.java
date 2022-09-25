package com.appgallabs.dataplatform.ingestion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class DataProcessor implements Runnable, Serializable
{
    private static Logger logger = LoggerFactory.getLogger(DataProcessor.class);
    private StreamReceiver streamReceiver;
    private List<String> activeQueueProcessors;

    public DataProcessor(StreamReceiver streamReceiver)
    {
        this.streamReceiver = streamReceiver;
        this.activeQueueProcessors = new ArrayList<>();
        System.out.println("***************"+this+"***************");
    }


    @Override
    public void run() {
        try {
            while(true) {
                Set<String> activeDataLakeIds = StreamIngesterContext.getStreamIngesterContext().activeDataLakeIds();

                for(String activeDataLakeId:activeDataLakeIds) {
                    if(!this.activeQueueProcessors.contains(activeDataLakeId)) {
                        Queue<StreamObject> queue = StreamIngesterContext.getStreamIngesterContext().getDataLakeQueue(activeDataLakeId);
                        if(!queue.isEmpty()) {
                            this.activeQueueProcessors.add(activeDataLakeId);
                            QueueProcessor queueProcessor = new QueueProcessor(activeDataLakeId, this.activeQueueProcessors, this.streamReceiver,
                                    queue);
                            Thread t = new Thread(queueProcessor);
                            t.start();
                        }
                    }
                }
            }
        } catch(Throwable t) {
            // restart if there is any other error
            logger.error(t.getMessage(),t);
            this.streamReceiver.restart(t.getMessage());
        }
    }
}
