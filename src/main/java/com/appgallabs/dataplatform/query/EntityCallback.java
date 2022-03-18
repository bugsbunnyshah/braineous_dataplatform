package com.appgallabs.dataplatform.query;

import com.google.gson.JsonObject;

public interface EntityCallback {
    void call(ObjectGraphQueryService queryService,String entityLabel,JsonObject entity);
}
