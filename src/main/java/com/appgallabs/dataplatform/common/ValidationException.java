package com.appgallabs.dataplatform.common;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;

public class ValidationException extends Exception{

    public ValidationException(String message) {
        super(message);
    }

    public JsonObject toJson(){
        String message = this.getMessage();
        JsonObject messageJson = JsonUtil.validateJson(message).getAsJsonObject();
        return messageJson;
    }
}
