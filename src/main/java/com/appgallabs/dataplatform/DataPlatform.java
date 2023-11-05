package com.appgallabs.dataplatform;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain(name="dataplatform")
public class DataPlatform {
    public static void main(String[] args) throws Exception
    {
        Quarkus.run(args);

        //Listener.getInstance().start();
    }
}
