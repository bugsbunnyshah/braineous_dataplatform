package com.appgallabs.dataplatform.cdc.engine;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CDCProcess {
    private static Logger logger = LoggerFactory.getLogger(CDCProcess.class);

    private static CDCProcess singleton = new CDCProcess();

    private CDCProcess() {
    }

    public static CDCProcess getInstance(){
        if(CDCProcess.singleton == null){
            CDCProcess.singleton = new CDCProcess();
        }
        return CDCProcess.singleton;
    }

    public void process(String input){
        //TODO: next
        System.out.println("debug_point");
        JsonObject inputJson = JsonUtil.validateJson(input).getAsJsonObject();

        JsonUtil.printStdOut(inputJson);
    }
}
