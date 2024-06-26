package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.configuration.ConfigurationService;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.CatalogDatabaseImpl;
import org.apache.flink.table.catalog.exceptions.DatabaseAlreadyExistException;
import org.apache.flink.table.catalog.hive.HiveCatalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class DataLakeSessionManager {
    private static Logger logger = LoggerFactory.getLogger(DataLakeSessionManager.class);

    @Inject
    private ConfigurationService configurationService;


    public String getHiveConfDirectory() {
        return this.configurationService.getProperty("hive_conf_directory");
    }

    public StreamTableEnvironment newDataLakeCatalogSession(StreamExecutionEnvironment env,
                                                            String catalog){
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        HiveCatalog hive = new HiveCatalog(catalog, null, this.getHiveConfDirectory());
        tableEnv.registerCatalog(catalog, hive);

        // set the HiveCatalog as the current catalog of the session
        tableEnv.useCatalog(catalog);

        return tableEnv;
    }

    public StreamTableEnvironment newDataLakeSessionWithNewDatabase(StreamExecutionEnvironment env,
                                                            String catalog,
                                                                    String database){
        try {
            final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

            HiveCatalog hive = new HiveCatalog(catalog, null, this.getHiveConfDirectory());
            tableEnv.registerCatalog(catalog, hive);

            // set the HiveCatalog as the current catalog of the session
            tableEnv.useCatalog(catalog);

            boolean databaseExists = hive.databaseExists(database);

            // Create a catalog database
            if(!databaseExists) {
                hive.createDatabase(database,
                        new CatalogDatabaseImpl(new HashMap<>(), "db_metadata"), true);
            }

            return tableEnv;
        }catch(DatabaseAlreadyExistException exists){
            return this.newDataLakeCatalogSession(env, catalog);
        }
    }
}
