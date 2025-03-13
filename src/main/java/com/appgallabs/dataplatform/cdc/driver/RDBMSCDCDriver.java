package com.appgallabs.dataplatform.cdc.driver;

import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.Statement;
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
}
