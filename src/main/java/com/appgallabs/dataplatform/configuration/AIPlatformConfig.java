package com.appgallabs.dataplatform.configuration;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class AIPlatformConfig {
    private static Logger logger = LoggerFactory.getLogger(AIPlatformConfig.class);

    private JsonObject configuration;

    @ConfigProperty(name = "mongodbHost")
    private String mongoDBHost;

    @ConfigProperty(name = "mongodbPort")
    private String mongoDBPort;

    @PostConstruct
    public void start()
    {
        try
        {
            //String mongoDBHost = System.getenv("MONGODBHOST");
            //String mongoDBPort = System.getenv("MONGODBPORT");
            String mongoDBUser = System.getenv("MONGODBUSER");
            String mongoDBPassword = System.getenv("MONGODBPASSWORD");

            this.configuration = new JsonObject();
            this.configuration.addProperty("mongodbHost", this.mongoDBHost);
            this.configuration.addProperty("mongodbPort", this.mongoDBPort);
            if(!StringUtils.isEmpty(mongoDBUser))
            {
                this.configuration.addProperty("mongodbUser", mongoDBUser);
            }
            if(!StringUtils.isEmpty(mongoDBPassword))
            {
                this.configuration.addProperty("mongodbPassword", mongoDBPassword);
            }
        }
        catch(Exception e)
        {
            this.configuration = null;
        }
    }

    public JsonObject getConfiguration()
    {
        return this.configuration;
    }
}
