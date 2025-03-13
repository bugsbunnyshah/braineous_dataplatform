package com.appgallabs.dataplatform.cdc.driver;

import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DBDataUtil {
    public static Map<String,String> query(JsonObject storeConfigJson, String sql) throws Exception{
        Map<String,String> result = new HashMap<>();

        Connection connection = null;
        Statement statement = null;
        try {
            connection = JDBCHelper.getInstance().getConnection(storeConfigJson);
            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while(resultSet.next()){
                for(int i=1; i<=columnCount; i++){
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    result.put(columnName, columnValue);
                }
            }

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
        return result;
    }
}
