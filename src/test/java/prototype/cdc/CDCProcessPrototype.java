package prototype.cdc;

import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;
import test.components.Util;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class CDCProcessPrototype implements Serializable {
    @Test
    public void processUpdates() throws Exception{
        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        final String[] dataKey = {"name", "email"};

        //get the change dataset
        String objStr = Util.loadResource("cdc/update_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonUtil.printStdOut(objJsonArray);

        this.runTest(
                configJsonStr,
                dataKey,
                objJsonArray
        );
    }

    @Test
    public void processInserts() throws Exception{
        final String configJsonStr = Util.loadResource("cdc/conf/db_config.json");
        final String[] dataKey = {"name", "email"};

        //get the change dataset
        String objStr = Util.loadResource("cdc/insert_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonUtil.printStdOut(objJsonArray);

        this.runTest(
                configJsonStr,
                dataKey,
                objJsonArray
        );
    }

    private void runTest(
            String configJsonStr,
            String[] dataKey,
            JsonArray objJsonArray
    ) throws Exception{
        Collection<String> objCollection = new ArrayList<>();
        for(int i=0; i<objJsonArray.size(); i++){
            JsonObject objJson = objJsonArray.get(i).getAsJsonObject();
            String objJsonStr = objJson.toString();
            objCollection.add(objJsonStr);
        }

        System.out.println(objCollection);

        //process each record in parallel to achieve O(1) time complexity using Flink
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> sourceData = env.fromCollection(
                objCollection
        );
        sourceData.print();

        DataStream<String> parallel = sourceData.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                //execute the CDC process/algorithm for each record
                JsonObject configJson = JsonUtil.validateJson(configJsonStr).getAsJsonObject();

                Map<String,String> dataKeyMap = new HashMap<>();
                for(int i=0; i<dataKey.length; i++){
                    dataKeyMap.put(dataKey[i],dataKey[i]);
                }

                //flatten
                JsonObject objJson = JsonUtil.validateJson(s).getAsJsonObject();
                Map<String, Object> objMap = JsonFlattener.flattenAsMap(objJson.toString());

                //construct a flat object
                JsonObject flatObject = new JsonObject();
                Set<Map.Entry<String,Object>> entrySet = objMap.entrySet();
                for(Map.Entry<String,Object> entry:entrySet){
                    String key = entry.getKey();
                    String value = entry.getValue().toString();

                    String field = key;
                    if(key.indexOf(".") != -1) {
                        int lastIndex = key.lastIndexOf('.');
                        field = key.substring(lastIndex+1);
                    }

                    flatObject.addProperty(field, value);
                }

                boolean isInsert = isInsert(configJson, dataKey, flatObject);
                if(isInsert){
                    //insert the record into the live target store
                    insertRecord(configJson, flatObject);
                }else{
                    //update the record in the live target store
                    updateRecord(configJson, flatObject);
                }

                return s;
            }
        });
        parallel.print();

        //execute the job graph
        env.execute();
    }
    //------------------------------------------------------------------------------------
    private boolean isInsert(JsonObject configJson, String[] dataKey, JsonObject record) throws Exception{
        Connection connection = null;
        Statement statement = null;
        try{
            String query = "select * from cdc_test";

            //where clause
            StringBuilder whereClause = new StringBuilder();
            for(int i=0; i<dataKey.length; i++){
                String columnName = dataKey[i];
                String value = record.get(columnName).getAsString();
                if(i < dataKey.length-1){
                    whereClause.append(columnName + "=" + "'" + value + "'" + " AND" + " ");
                }else{
                    whereClause.append(columnName + "=" + "'" + value + "'");
                }
            }

            query += " " + "where" + " " + whereClause.toString();

            connection = JDBCHelper.getInstance().getConnection(configJson);
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()){
                //record exists and hence update operation
                return false;
            }else{
                //record does not exist hence insert opertion
                return true;
            }
        }finally{
            if(connection != null){
                try{connection.close();}catch(Exception e){}
            }
            if(statement != null){
                try{statement.close();}catch(Exception e){}
            }
        }
    }

    private void insertRecord(JsonObject configJson, JsonObject record) throws Exception{
        System.out.println("debug_point");
        System.out.println("*****INSERT_RECORD*****");

        Connection connection = null;
        Statement statement = null;
        try{
            String sql = "insert into cdc_test ";
            sql += this.generateInsertSql(record);

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

    private void updateRecord(JsonObject configJson, JsonObject record) throws Exception{
        System.out.println("debug_point");
        System.out.println("*****UPDATE_RECORD*****");

        Connection connection = null;
        Statement statement = null;
        try{
            String sql = "update cdc_test ";
            sql += this.generateUpdateSql(record);

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

    private String generateInsertSql(JsonObject record){
        String sql = null;

        //flatten
        Map<String,Object> structuredData = JsonFlattener.flattenAsMap(record.toString());

        Set<Map.Entry<String, Object>> entrySet = structuredData.entrySet();
        StringBuilder columns = new StringBuilder("");
        StringBuilder values = new StringBuilder("");
        for(Map.Entry<String, Object> entry: entrySet){
            String columnName = entry.getKey();
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

    private String generateUpdateSql(JsonObject record){
        String sql = null;

        //flatten
        Map<String,Object> structuredData = JsonFlattener.flattenAsMap(record.toString());

        Set<Map.Entry<String, Object>> entrySet = structuredData.entrySet();
        StringBuilder values = new StringBuilder("");
        for(Map.Entry<String, Object> entry: entrySet){
            String columnName = entry.getKey();
            String value = entry.getValue().toString();
            values.append(columnName + "=" + "'" + value + "'" + ",");
        }

        String valueToken = values.toString();
        valueToken = valueToken.substring(0, valueToken.length()-1);

        String valuesString = values.toString();
        valuesString = valuesString.substring(0, valuesString.length()-1);

        sql = "set" + " " + valueToken;

        return sql;
    }
}
