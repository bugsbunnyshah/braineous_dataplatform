package com.appgallabs.dataplatform.configuration;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigurationService {
    private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    String confLocation = "conf/braineous.config";

    private Configuration config;

    public ConfigurationService() {
    }

    @PostConstruct
    public void start(){
        this.configure();
    }

    public void configure(){
        try {
            String confLocation = this.findConfLocation();
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(confLocation));
            this.config = builder.getConfiguration();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String property){
        return this.config.getProperty(property).toString();
    }

    private String findConfLocation(){

        //TODO: (NOW)
        return this.confLocation;
    }
}
