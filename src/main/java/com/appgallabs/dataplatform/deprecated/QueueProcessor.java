package com.appgallabs.dataplatform.deprecated;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Queue;

public class QueueProcessor implements Runnable{
    private String dataLakeId;
    private Queue<StreamObject> queue;
    private StreamReceiver streamReceiver;
    private List<String> activeProcessors;

    public QueueProcessor(String dataLakeId, List<String> activeProcessors, StreamReceiver streamReceiver, Queue<StreamObject> queue){
        this.dataLakeId = dataLakeId;
        this.activeProcessors = activeProcessors;
        this.streamReceiver = streamReceiver;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (!this.queue.isEmpty()) {
                StreamObject streamObject = this.queue.poll();
                //JsonUtil.printStdOut(streamObject.toJson());
                if(streamObject != null) {
                    JsonObject jsonObject = streamObject.toJson();
                    if(this.streamReceiver.isStarted()) {
                        System.out.println("SUBMITTING_TO_SPARK");
                        this.streamReceiver.store(jsonObject.toString());
                    }
                }
            }
        }
        finally {
            this.activeProcessors.remove(this.dataLakeId);
        }
    }
}
