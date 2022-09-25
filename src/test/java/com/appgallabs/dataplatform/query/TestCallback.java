package com.appgallabs.dataplatform.query;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TestCallback implements EntityCallback{

    @Override
    public void call(ObjectGraphQueryService queryService, String entityLabel, JsonArray entity) {
        /*String relatedTo = "airport";
        String left = "departure";
        String right = "arrival";

        //Departure
        JsonObject departure = entity.get("departure").getAsJsonObject();
        JsonObject departureAirport = new JsonObject();
        departureAirport.addProperty("name",departure.get("airport").getAsString());
        queryService.saveObjectRelationship(relatedTo,departureAirport);

        //Arrival
        JsonObject arrival = entity.get("arrival").getAsJsonObject();
        JsonObject arrivalAirport = new JsonObject();
        arrivalAirport.addProperty("name",arrival.get("airport").getAsString());
        queryService.saveObjectRelationship(relatedTo,arrivalAirport);

        //establish
        queryService.establishRelationship(entityLabel,relatedTo,left);
        queryService.establishRelationship(entityLabel,relatedTo,right);*/
    }
}
