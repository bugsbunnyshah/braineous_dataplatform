package com.appgallabs.dataplatform.util;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BGNotificationReceiver implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(BGNotificationReceiver.class);

    private JsonArray data;

    public BGNotificationReceiver() {
        this.data = new JsonArray();
    }

    public JsonArray getData() {
        return data;
    }
}
