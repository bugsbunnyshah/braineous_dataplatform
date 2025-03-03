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
                JsonObject left = this.matchSourceData(sourceData.get(0).getAsJsonObject());
                String objectHash = JsonUtil.getJsonHash(left);

                if (this.isInsert(left, destinationData)) {
                    inserts.put(objectHash, left);
                    break;
                }else if(this.isUpdate(left, destinationData)){
                    updates.put(objectHash, left);
                    break;
                }else if(this.isDelete(left, destinationData)){
                    deletes.put(objectHash, left);
                    break;
                }
            }

            //print results
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private JsonArray getDestinationData(JsonObject configJson){
        JsonArray jsonArray = new JsonArray();

        return jsonArray;
    }

    private JsonObject matchSourceData(JsonObject jsonObject){
        return null;
    }

    public boolean isInsert(JsonObject sourceObject, JsonArray destinationData){
        return false;
    }

    public boolean isUpdate(JsonObject sourceObject, JsonArray destinationData){
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
