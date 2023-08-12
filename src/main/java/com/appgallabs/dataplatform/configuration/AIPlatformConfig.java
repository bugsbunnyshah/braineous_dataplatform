package com.appgallabs.dataplatform.configuration;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class AIPlatformConfig implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(AIPlatformConfig.class);

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


    public JsonObject getConfiguration()
    {
        JsonObject configuration = new JsonObject();
        configuration.addProperty("mongodbHost", this.mongoDBHost);
        configuration.addProperty("mongodbPort", this.mongoDBPort);
        configuration.addProperty("queryServiceUri", this.queryServiceUri);
        configuration.addProperty("queryServiceUser", this.queryServiceUser);
        configuration.addProperty("queryServicePassword", this.queryServicePassword);
        return configuration;
    }
}
