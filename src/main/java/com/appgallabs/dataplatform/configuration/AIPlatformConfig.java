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

    @ConfigProperty(name = "queryServiceUri")
    private String queryServiceUri;

    @ConfigProperty(name = "queryServiceUser")
    private String queryServiceUser;

    @ConfigProperty(name = "queryServicePassword")
    private String queryServicePassword;

    @PostConstruct
    public void start()
    {
        try
        {
            this.configuration = new JsonObject();
            this.configuration.addProperty("mongodbHost", this.mongoDBHost);
            this.configuration.addProperty("mongodbPort", this.mongoDBPort);
            this.configuration.addProperty("queryServiceUri", this.queryServiceUri);
            this.configuration.addProperty("queryServiceUser", this.queryServiceUser);
            this.configuration.addProperty("queryServicePassword", this.queryServicePassword);
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
