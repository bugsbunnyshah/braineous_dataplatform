package com.appgallabs.dataplatform.ingestion.service;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

public class StreamReceiver extends Receiver<String> {
    private DataProcessor dataProcessor;

    public StreamReceiver(StorageLevel storageLevel) {
        super(storageLevel);
        this.dataProcessor = new DataProcessor(this);
        System.out.println("***************"+this+"***************");
    }

    @Override
    public void onStart() {
        try {
            // Start the thread that receives data over a connection
            Thread t = new Thread(this.dataProcessor);
            t.start();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStop() {
        // There is nothing much to do as the thread calling receive()
        // is designed to stop by itself if isStopped() returns false
    }
}
