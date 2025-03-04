package prototype.cdc;

import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.appgallabs.dataplatform.util.JsonUtil;

import com.github.wnameless.json.flattener.JsonFlattener;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;
import test.components.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
        try {
            //get the destination data
            JsonArray destinationData = this.getDestinationData(configJson);

            //create insert bucket
            Map<String, JsonObject> inserts = new HashMap<>();

            //create update bucket
            Map<String, JsonObject> updates = new HashMap();

            //create delete bucket
            Map<String, JsonObject> deletes = new HashMap<>();

            //find the right bucket
            for (int i = 0; i < sourceData.size(); i++) {
                JsonObject left = this.matchSourceData(sourceData.get(i).getAsJsonObject());
                String objectHash = JsonUtil.getJsonHash(left);

                if (this.isInsert(left, objectHash, destinationData)) {
                    inserts.put(objectHash, left);
                }else if(this.isUpdate(left, destinationData)){
                    updates.put(objectHash, left);
                }

                //Delete operation not applicable in a agnostic data ingestion scenario
                /*else if(this.isDelete(left, destinationData)){
                    deletes.put(objectHash, left);
                }*/
            }

            //print results
            System.out.println("*****INSERTS***********");
            JsonUtil.printStdOut(JsonUtil.validateJson(inserts.toString()));

            System.out.println("******UPDATES***********");
            JsonUtil.printStdOut(JsonUtil.validateJson(updates.toString()));

            System.out.println("******DELETES***********");
            JsonUtil.printStdOut(JsonUtil.validateJson(deletes.toString()));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private JsonArray getDestinationData(JsonObject configJson) throws Exception{
        JsonArray jsonArray = new JsonArray();
        Connection connection = null;
        Statement statement = null;
        try{
            String query = "select * from cdc_test";
            connection = JDBCHelper.getInstance().getConnection(configJson);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while(resultSet.next()){
                JsonObject record = new JsonObject();
                for(int i=1; i<=columnCount; i++){
                   String columnName = metaData.getColumnName(i);
                   String columnValue = resultSet.getString(i);
                   record.addProperty(columnName, columnValue);
                }
                jsonArray.add(record);
            }
        }finally{
            if(connection != null){
                try{connection.close();}catch(Exception e){}
            }
            if(statement != null){
                try{statement.close();}catch(Exception e){}
            }
        }
        return jsonArray;
    }

    private JsonObject matchSourceData(JsonObject jsonObject){
        JsonObject matchedSourceData = new JsonObject();

        //flatten
        Map<String, Object> objMap = JsonFlattener.flattenAsMap(jsonObject.toString());

        Set<Map.Entry<String,Object>> entrySet = objMap.entrySet();
        for(Map.Entry<String,Object> property:entrySet){
            String name = property.getKey();
            if(name.indexOf(".") != -1) {
                int lastIndex = name.lastIndexOf('.');
                name = name.substring(lastIndex+1);
            }

            String value = property.getValue().toString();

            matchedSourceData.addProperty(name, value);
        }

        return matchedSourceData;
    }

    public boolean isInsert(JsonObject sourceObject,String sourceHash, JsonArray destinationData) throws Exception
    {
        for(int i=0; i<destinationData.size(); i++){
            JsonObject destinationObject = destinationData.get(i).getAsJsonObject();
            String destinationHash = JsonUtil.getJsonHash(destinationObject);
            if(sourceHash.equals(destinationHash)){
                //object already exists in the store. avoid duplication
                return false;
            }
        }
        return true;
    }

    public boolean isUpdate(JsonObject sourceObject, JsonArray destinationData){
        //TODO: need concept of object identity and not object hash
        return false;
    }

    public boolean isDelete(JsonObject sourceObject, JsonArray destinationData){
        return false;
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
