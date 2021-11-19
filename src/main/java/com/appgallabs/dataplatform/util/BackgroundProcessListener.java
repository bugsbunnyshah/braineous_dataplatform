package com.appgallabs.dataplatform.util;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundProcessListener {
    private static Logger logger = LoggerFactory.getLogger(BackgroundProcessListener.class);

    private static BackgroundProcessListener singleton=null;

    public static BackgroundProcessListener getInstance(){
        if(singleton == null){
            singleton = new BackgroundProcessListener();
        }
        return BackgroundProcessListener.singleton;
    }

    private int threshold;
    private BGNotificationReceiver receiver;
    private String dataLakeId;
    private boolean found = false;

    public BackgroundProcessListener(){
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public BGNotificationReceiver getReceiver() {
        return receiver;
    }

    public String getDataLakeId() {
        return dataLakeId;
    }

    public void setDataLakeId(String dataLakeId) {
        this.dataLakeId = dataLakeId;
    }

    public void setReceiver(BGNotificationReceiver receiver) {
        this.receiver = receiver;
    }

    public void decreaseThreshold(JsonObject jsonObject){
        this.threshold--;

        if(this.receiver != null && this.receiver.getData() != null) {
            this.receiver.getData().add(jsonObject);
            //JsonUtil.print(this.receiver.getData());
            String local = jsonObject.get("braineous_datalakeid").getAsString();
            if(this.dataLakeId != null){
                if(this.dataLakeId.equals(local)){
                    found = true;
                }
            }
        }

        if(this.threshold == 0 && found){
            this.notifyReceiver();
        }
    }

    public void clear(){
        BackgroundProcessListener.singleton = null;
    }
//-----------------------------------------------------
    private void notifyReceiver(){
        if(this.receiver != null){
            synchronized (this.receiver) {
                this.receiver.notifyAll();
            }
        }
    }
}
