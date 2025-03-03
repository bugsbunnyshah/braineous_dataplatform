package prototype.cdc;

import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.github.wnameless.json.flattener.JsonFlattener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;
import test.components.Util;

import java.sql.Connection;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.*;

public class AlgorithmTests {

    @Test
    public void mapToStructured() throws Exception{
        List<Map<String,String>> structuredData = new ArrayList<>();

        //get the dataset
        String objStr = Util.loadResource("cdc/obj1_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();

        for(int i=0; i<objJsonArray.size(); i++) {
            JsonObject objJson = objJsonArray.get(i).getAsJsonObject();

            //flatten
            Map<String, Object> objMap = JsonFlattener.flattenAsMap(objJson.toString());

            Map<String,String> row = new HashMap<>();
            Set<Map.Entry<String,Object>> entrySet = objMap.entrySet();
            for(Map.Entry<String, Object> entry:entrySet){
                String columnName = entry.getKey();
                String value = entry.getValue().toString();

                row.put(columnName, value);
            }
            structuredData.add(row);
        }

        //get sqls
        List<String> sqls = this.generateSql(structuredData);
        System.out.println(sqls);

        String table = "cdc_test";
        for(String sql: sqls){
            this.executeSql(table, sql);
        }

        //execute CDC Algorithm
        String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        JsonObject configJson = JsonUtil.validateJson(configJsonStr).getAsJsonObject();
        this.executeCDCAlgorithm(configJson, objJsonArray);
    }

    private void executeCDCAlgorithm(JsonObject configJson, JsonArray sourceData){

    }

    //-------------------------------------------------------------
    private List<String> generateSql(List<Map<String,String>> structuredData){
        List<String> sqls = new ArrayList<>();

        for(Map<String,String> rows: structuredData) {
            Set<Map.Entry<String, String>> entrySet = rows.entrySet();

            StringBuilder columns = new StringBuilder("");
            StringBuilder values = new StringBuilder("");

            for(Map.Entry<String,String> row: entrySet) {
                String columnName = row.getKey();
                if(columnName.indexOf(".") != -1) {
                    int lastIndex = columnName.lastIndexOf('.');
                    columnName = columnName.substring(lastIndex+1);
                }
                String value = row.getValue();

                columns.append(columnName + ",");
                values.append("'" + value + "'" + ",");
            }

            String columnsString = columns.toString();
            columnsString = columnsString.substring(0, columnsString.length()-1);

            String valuesString = values.toString();
            valuesString = valuesString.substring(0, valuesString.length()-1);

            String sql = "(" + columnsString + ") VALUES (" + valuesString +")";
            sqls.add(sql);
        }

        return sqls;
    }

    private void executeSql(String table, String sql) throws Exception{
        String executeSql = "INSERT INTO " + table + " ";
        executeSql += sql;
        System.out.println("**********************");
        System.out.println(executeSql);

        String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        JsonObject configJson = JsonUtil.validateJson(configJsonStr).getAsJsonObject();
        this.insertRecord(configJson, executeSql);
    }

    private void insertRecord(JsonObject configJson, String sql) throws Exception{
        Connection connection = null;
        Statement statement = null;
        try{
            connection = JDBCHelper.getInstance().getConnection(configJson);
            statement = connection.createStatement();
            statement.executeUpdate(sql);
        }finally{
            if(connection != null){
                try{connection.close();}catch(Exception e){}
            }
            if(statement != null){
                try{statement.close();}catch(Exception e){}
            }
        }
    }
}
