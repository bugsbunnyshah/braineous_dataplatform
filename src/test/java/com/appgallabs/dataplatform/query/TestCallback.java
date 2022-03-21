package com.appgallabs.dataplatform.query;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;

public class TestCallback implements EntityCallback{

    @Override
    public void call(ObjectGraphQueryService queryService,String entityLabel,JsonObject entity) {
        JsonObject departure = entity.get("departure").getAsJsonObject();
        String airport = departure.get("airport").getAsString();
        JsonObject newEntity = new JsonObject();
        newEntity.addProperty("name",airport);

        String newEntityLabel = "airport";
        String relationship = "departure";
        queryService.saveObjectRelationship(newEntityLabel,newEntity);
        queryService.establishRelationship(entityLabel,newEntityLabel,relationship);
    }
}
