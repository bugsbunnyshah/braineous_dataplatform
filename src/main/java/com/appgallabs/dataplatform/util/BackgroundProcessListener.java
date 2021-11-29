package com.appgallabs.dataplatform.util;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private boolean found = false;

    private Map<String, Set<String>> entityToIdMap;

    public BackgroundProcessListener(){
        this.entityToIdMap = new HashMap<>();
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

    public void setReceiver(BGNotificationReceiver receiver) {
        this.receiver = receiver;
    }

    public void decreaseThreshold(String entity,String dataLakeId,JsonObject jsonObject){
        if(entity.equals("not_specified")){
            System.out.println(jsonObject);
            return;
        }

        if(this.entityToIdMap.get(entity) == null){
            this.entityToIdMap.put(entity,new HashSet<>());
        }
        this.entityToIdMap.get(entity).add(dataLakeId);

        this.threshold--;

        if(this.receiver != null && this.receiver.getData() != null) {
            this.receiver.getData().add(jsonObject);
            JsonUtil.print(this.receiver.getData());
            String local = jsonObject.get("braineous_datalakeid").getAsString();
            if(this.entityToIdMap.get(entity).contains(local))
            {
                found = true;
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
