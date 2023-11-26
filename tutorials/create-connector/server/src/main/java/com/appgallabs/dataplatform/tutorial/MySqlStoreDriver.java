package com.appgallabs.dataplatform.tutorial;

import com.appgallabs.dataplatform.targetSystem.framework.StoreDriver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class MySqlStoreDriver implements StoreDriver {
    private static Logger logger = LoggerFactory.getLogger(MySqlStoreDriver.class);

    private Connection connection;
    private JsonObject configJson;

    @Override
    public void configure(JsonObject configJson) {
        try {
            this.configJson = configJson;

            Statement createTableStatement = null;
            try {
                String url = configJson.get("connectionString").getAsString();
                String username = configJson.get("username").getAsString();

                String password = configJson.get("password").getAsString();

                this.connection = DriverManager.getConnection(
                        url, username, password);
                System.out.println(
                        "Connection Established successfully");

                //create schema and tables
                String createTableSql = "CREATE TABLE IF NOT EXISTS staged_data (\n" +
                        "    id int NOT NULL AUTO_INCREMENT,\n" +
                        "    data varchar(255) NOT NULL,\n" +
                        "    PRIMARY KEY (id)\n" +
                        ")";
                createTableStatement = this.connection.createStatement();
                createTableStatement.executeUpdate(createTableSql);

                System.out.println("Created table in given database...");

            } finally {
                createTableStatement.close();

                System.out.println("****MYSQL_CONNECTOR_SUCCESSFULLY_REGISTERED*****");
                System.out.println("****BRING_THE_HEAT (lol)*****");
            }
        }catch(Exception e){
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service
        }
    }

    @Override
    public void storeData(JsonArray dataSet) {
        try {
            try {
                String query = "select * from staged_data";

                //populate table
                int size = dataSet.size();
                for (int i = 0; i < size; i++) {
                    JsonElement record = dataSet.get(i);
                    String insertSql = "insert into staged_data (data) values ('" + record.toString() + "')";
                    Statement insertStatement = this.connection.createStatement();
                    insertStatement.executeUpdate(insertSql);
                    insertStatement.close();
                }

                Statement queryStatement = this.connection.createStatement();
                ResultSet rs = queryStatement.executeQuery(query);
                while (rs.next()) {
                    String id = rs.getString("id");
                    String data = rs.getString("data");
                    System.out.println(id);
                    System.out.println(data);
                    System.out.println("***************");
                }
                queryStatement.close();

                System.out.println("Connection Closed....");
            } finally {
                this.connection.close();
            }
        }catch(Exception e){
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service
        }
    }
}
