package com.appgallabs.dataplatform;

import com.appgallabs.dataplatform.deprecated.StreamIngesterContext;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain(name="dataplatform")
public class DataPlatform {
    public static void main(String[] args) throws Exception
    {
        //TODO CR1
        System.out.println("Braineous DataPlatform started...");
        StreamIngesterContext.getStreamIngester().start();
        Quarkus.run(args);
    }
}
