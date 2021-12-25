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
        StreamIngesterContext.getStreamIngester().start();
        Quarkus.run(args);
    }
}
