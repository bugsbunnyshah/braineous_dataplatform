package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.targetSystem.framework.staging.IntegrationRunner;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class CustomIntegrationRunner implements IntegrationRunner {
    private static Logger logger = LoggerFactory.getLogger(CustomIntegrationRunner.class);

    private Connection connection;
    private String liveTable = "liveTable";

    @Override
    public void preProcess(Tenant tenant, String pipeId, String entity) {
        try {
            Statement createTableStatement = null;
            try {
                String url = "jdbc:mysql://localhost:3306/braineous_staging_database";
                String username = "root";
                String password = "";

                this.connection = DriverManager.getConnection(
                        url, username, password);

                //create schema and tables
                String createTableSql = "CREATE TABLE IF NOT EXISTS "+this.liveTable+" (\n" +
                        "    id int NOT NULL AUTO_INCREMENT,\n" +
                        "    name longtext NOT NULL,\n" +
                        "    age longtext NOT NULL,\n" +
                        "    PRIMARY KEY (id)\n" +
                        ")";
                createTableStatement = this.connection.createStatement();
                createTableStatement.executeUpdate(createTableSql);

            } finally {
                createTableStatement.close();
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public void process(Tenant tenant, String pipeId, String entity, List<Record> records) {
        try {
            Statement insertStatement = this.connection.createStatement();
            try {
                //populate table
                for (Record record: records) {
                    JsonObject recordJson = record.toJson();
                    JsonUtil.printStdOut(recordJson);

                    String name = recordJson.get("data").getAsJsonObject().get("name").getAsString();
                    String age = recordJson.get("data").getAsJsonObject().get("age").getAsString();;

                    String insertSql = "insert into "+this.liveTable+" (name,age) " +
                            "values " +
                            "('"+name+"'," +
                            "'"+age+"'" +
                            ")";

                    insertStatement.addBatch(insertSql);
                }

                insertStatement.executeBatch();

            } finally {
                insertStatement.close();
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public void postProcess(Tenant tenant, String pipeId, String entity) {
        try {
            this.connection.close();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
}
