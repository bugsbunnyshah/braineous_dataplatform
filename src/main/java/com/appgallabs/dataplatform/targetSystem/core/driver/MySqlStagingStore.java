package com.appgallabs.dataplatform.targetSystem.core.driver;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;

import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class MySqlStagingStore implements StagingStore {
    private static Logger logger = LoggerFactory.getLogger(MySqlStagingStore.class);

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

                //create schema and tables
                String createTableSql = "CREATE TABLE IF NOT EXISTS staged_data (\n" +
                        "    id int NOT NULL AUTO_INCREMENT,\n" +
                        "    data longtext NOT NULL,\n" +
                        "    PRIMARY KEY (id)\n" +
                        ")";
                createTableStatement = this.connection.createStatement();
                createTableStatement.executeUpdate(createTableSql);

                //System.out.println("Created table in given database...");

            } finally {
                createTableStatement.close();
            }
        }catch(Exception e){
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service
        }
    }

    @Override
    public String getName() {
        return this.configJson.get("connectionString").getAsString();
    }

    @Override
    public JsonObject getConfiguration() {
        return this.configJson;
    }

    @Override
    public void storeData(Tenant tenant, String pipeId, String entity, List<Record> records) {
        JsonArray dataSet = new JsonArray();
        for(Record record: records){
            JsonObject data = record.getData();
            dataSet.add(data);
        }
        this.storeData(dataSet);
    }

    @Override
    public List<Record> getData(Tenant tenant, String pipeId, String entity) {
        return null;
    }

    //----------------------------------------------------------------------------------------------
    private void storeData(JsonArray dataSet) {
        try {
            Statement insertStatement = this.connection.createStatement();
            try {
                //populate table
                int size = dataSet.size();
                for (int i = 0; i < size; i++) {
                    JsonElement record = dataSet.get(i);
                    String insertSql = "insert into staged_data (data) values ('" + record.toString() + "')";
                    insertStatement.addBatch(insertSql);
                }

                insertStatement.executeBatch();

                /*String query = "SELECT count(*) FROM staged_data;";
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
                */

            } finally {
                insertStatement.close();
                this.connection.close();
                System.out.println(
                        "MYSQL: STORED_SUCCESSFULLY");
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            //TODO: (CR2) report to the pipeline monitoring service
        }
    }
}
