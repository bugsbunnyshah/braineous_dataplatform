package com.appgallabs.dataplatform.cdc.driver;

import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RDBMSCDCDriver implements CDCDriver {
    private static RDBMSCDCDriver singleton = new RDBMSCDCDriver();

    private RDBMSCDCDriver() {
    }

    public static RDBMSCDCDriver getInstance(){
        if(RDBMSCDCDriver.singleton == null){
            RDBMSCDCDriver.singleton = new RDBMSCDCDriver();
        }
        return RDBMSCDCDriver.singleton;
    }

    @Override
    public void insert(JsonObject storeConfigJson, CDCDataContext cdcDataContext) {
        try {
            String table = cdcDataContext.getTable();
            JsonObject record = cdcDataContext.getRecord();

            Connection connection = null;
            Statement statement = null;
            try {
                String sql = "insert into" + " " + table + " ";
                sql += this.generateInsertSql(record);

                connection = JDBCHelper.getInstance().getConnection(storeConfigJson);
                statement = connection.createStatement();
                statement.executeUpdate(sql);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                    }
                }

                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                    }
                }
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public int update(JsonObject storeConfigJson, CDCDataContext cdcDataContext) {
        try{
            String table = cdcDataContext.getTable();
            JsonObject record = cdcDataContext.getRecord();

            //dataKey
            JsonArray dataKeyArray = storeConfigJson.getAsJsonArray("data_key");
            Map<String, String> dataKeyMap = new HashMap<>();
            for (int i = 0; i < dataKeyArray.size(); i++) {
                String dataKey = dataKeyArray.get(i).getAsString();
                dataKeyMap.put(dataKey, dataKey);
            }

            int updateCount = 0;

            Connection connection = null;
            Statement statement = null;
            try {
                String sql = "update" + " " + table + " ";
                sql += this.generateUpdateSql(record, dataKeyMap);

                connection = JDBCHelper.getInstance().getConnection(storeConfigJson);
                statement = connection.createStatement();
                updateCount = statement.executeUpdate(sql);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                    }
                }

                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                    }
                }
            }
            return updateCount;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    //--------------------------------------------------------------------------------
    private String generateInsertSql(JsonObject record){
        String sql = null;

        //flatten
        Map<String,Object> structuredData = JsonFlattener.flattenAsMap(record.toString());

        Set<Map.Entry<String, Object>> entrySet = structuredData.entrySet();
        StringBuilder columns = new StringBuilder("");
        StringBuilder values = new StringBuilder("");
        for(Map.Entry<String, Object> entry: entrySet){
            String columnName = entry.getKey();
            if(columnName.indexOf(".") != -1) {
                int lastIndex = columnName.lastIndexOf('.');
                columnName = columnName.substring(lastIndex+1);
            }

            String value = entry.getValue().toString();

            columns.append(columnName + ",");
            values.append("'" + value + "'" + ",");
        }

        String columnsString = columns.toString();
        columnsString = columnsString.substring(0, columnsString.length()-1);

        String valuesString = values.toString();
        valuesString = valuesString.substring(0, valuesString.length()-1);

        sql = "(" + columnsString + ") values (" + valuesString +")";

        return sql;
    }

    private String generateUpdateSql(JsonObject record, Map<String, String> dataKeyMap){
        String sql = null;

        //flatten
        Map<String,Object> structuredData = JsonFlattener.flattenAsMap(record.toString());

        Set<Map.Entry<String, Object>> entrySet = structuredData.entrySet();
        StringBuilder values = new StringBuilder("");
        StringBuilder whereClause = new StringBuilder("");
        for(Map.Entry<String, Object> entry: entrySet){
            String columnName = entry.getKey();
            if(columnName.indexOf(".") != -1) {
                int lastIndex = columnName.lastIndexOf('.');
                columnName = columnName.substring(lastIndex+1);
            }

            String value = entry.getValue().toString();
            values.append(columnName + "=" + "'" + value + "'" + ",");

            if(dataKeyMap.containsKey(columnName)) {
                whereClause.append(columnName + "=" + "'" + value + "'" + " AND" + " ");
            }
        }

        String valueToken = values.toString();
        valueToken = valueToken.substring(0, valueToken.length()-1);

        String whereToken = whereClause.toString();
        whereToken = whereToken.substring(0, whereToken.lastIndexOf(" AND"));

        sql = "set" + " " + valueToken + " " + "where" + " " + whereToken;

        return sql;
    }
}
