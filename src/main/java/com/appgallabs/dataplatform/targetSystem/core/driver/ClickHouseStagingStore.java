package com.appgallabs.dataplatform.targetSystem.core.driver;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.reporting.IngestionReportingService;
import com.appgallabs.dataplatform.targetSystem.framework.staging.Record;
import com.appgallabs.dataplatform.targetSystem.framework.staging.StagingStore;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

public class ClickHouseStagingStore implements StagingStore {
    private static Logger logger = LoggerFactory.getLogger(ClickHouseStagingStore.class);

    private Connection connection;
    private JsonObject configJson;

    //TODO: (NOW) - thread it in
    private IngestionReportingService ingestionReportingService;

    @Override
    public void configure(JsonObject configJson) {
        try {
            this.configJson = configJson;
            Statement createTableStatement = null;
            try {
                String url = configJson.get("connectionString").getAsString();
                String username = configJson.get("username").getAsString();
                String password = configJson.get("password").getAsString();

                Properties properties = new Properties();
                ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);

                this.connection = dataSource.getConnection(username, password);

                //create schema and tables
                String createTableSql = "CREATE TABLE IF NOT EXISTS staged_data\n" +
                        "        (\n" +
                        "                id String,\n" +
                        "                data String\n" +
                        "        )\n" +
                        "        ENGINE = MergeTree";
                createTableStatement = this.connection.createStatement();
                createTableStatement.executeUpdate(createTableSql);

                //System.out.println("Created table in given database...");

            } finally {
                createTableStatement.close();
            }
        }catch(Exception e){
            logger.error(e.getMessage());

            //report to the pipeline monitoring service
            //JsonObject jsonObject = new JsonObject();
            //this.ingestionReportingService.reportDataError(jsonObject);
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
                    String id = JsonUtil.getJsonHash(record.getAsJsonObject());

                    String insertSql = "insert into staged_data (id, data) values ('"+id+"','" + record.toString() + "')";
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

                System.out.println(
                        "CLICKHOUSE: DATA_STORED_SUCCESSFULLY");

            } finally {
                insertStatement.close();
                this.connection.close();
            }
        }catch(Exception e){
            logger.error(e.getMessage());

            //report to the pipeline monitoring service
            //JsonObject jsonObject = new JsonObject();
            //this.ingestionReportingService.reportDataError(jsonObject);
        }
    }
}
