package com.appgallabs.dataplatform;

import com.appgallabs.dataplatform.ingestion.service.StreamIngesterContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain(name="dataplatform")
public class DataPlatform {
    public static void main(String[] args) throws Exception
    {
        System.out.println("Braineous DataPlatform started...");
        System.out.println("*******************************");
        System.out.println("STARTING_INGESTION");
        System.out.println("*******************************");
        JsonArray array = new JsonArray();
        array.add(new JsonObject());
        StreamIngesterContext.getStreamIngester().submit(null,null, null,null,array);


        Quarkus.run(args);
    }
}
