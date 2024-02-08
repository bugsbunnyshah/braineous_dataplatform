package prototype.ingestion;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigurationService {
    private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private Configuration config;

    public ConfigurationService() {
    }

    public void configure(String confLocation){
        try {
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
}
