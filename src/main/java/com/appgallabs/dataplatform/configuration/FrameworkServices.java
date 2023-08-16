package com.appgallabs.dataplatform.configuration;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class FrameworkServices implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(FrameworkServices.class);

    private Gson gson;

    @PostConstruct
    public void start(){
       this.gson = new Gson();
    }

    public Gson getGson() {
        return gson;
    }
}
