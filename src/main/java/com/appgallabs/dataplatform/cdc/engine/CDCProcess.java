package com.appgallabs.dataplatform.cdc.engine;

import com.appgallabs.dataplatform.cdc.driver.CDCDataContext;
import com.appgallabs.dataplatform.cdc.driver.CDCDriver;
import com.appgallabs.dataplatform.cdc.driver.RDBMSCDCDriver;
import com.appgallabs.dataplatform.infrastructure.JDBCHelper;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class CDCProcess {
    private static Logger logger = LoggerFactory.getLogger(CDCProcess.class);

    private static CDCProcess singleton = new CDCProcess();

    private CDCProcess() {
    }

    public static CDCProcess getInstance(){
        if(CDCProcess.singleton == null){
            CDCProcess.singleton = new CDCProcess();
        }
        return CDCProcess.singleton;
    }

    public void process(String input){
        try {
            JsonObject inputJson = JsonUtil.validateJson(input).getAsJsonObject();

            //cdcConfig
            JsonObject cdcConfig = inputJson.getAsJsonObject("cdcConfig");

            //record
            JsonObject record = inputJson.getAsJsonObject("record");

            //dataKey
            JsonArray dataKeyArray = cdcConfig.getAsJsonArray("data_key");
            Map<String, String> dataKeyMap = new HashMap<>();
            for (int i = 0; i < dataKeyArray.size(); i++) {
                String dataKey = dataKeyArray.get(i).getAsString();
                dataKeyMap.put(dataKey, dataKey);
            }

            //flatten
            Map<String, Object> recordMap = JsonFlattener.flattenAsMap(record.toString());

            //construct a flat object
            JsonObject flatObject = new JsonObject();
            Set<Map.Entry<String, Object>> entrySet = recordMap.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                String key = entry.getKey();
                String value = entry.getValue().toString();

                String field = key;
                if (key.indexOf(".") != -1) {
                    int lastIndex = key.lastIndexOf('.');
                    field = key.substring(lastIndex + 1);
                }

                flatObject.addProperty(field, value);
            }

            boolean isInsert = isInsert(cdcConfig, dataKeyArray, flatObject);

            //TODO: choose the driver based on target store
            CDCDriver cdcDriver = RDBMSCDCDriver.getInstance();
            String table = cdcConfig.get("table").getAsString();
            CDCDataContext dataContext = new CDCDataContext();
            dataContext.setTable(table);
            dataContext.setRecord(record);
            if(isInsert){
                //insert the record into the live target store
                cdcDriver.insert(
                        cdcConfig,
                        dataContext
                );
            }else{
                //update the record in the live target store
                int updateCount = cdcDriver.update(
                        cdcConfig,
                        dataContext
                );

                //TODO: pipeline_cdc_processing_report
            }
        }catch(Exception e){
            //TODO: pipeline_error_report

            throw new RuntimeException(e);
        }
    }
    //--------------------------------------------------------------------------------------------
    private boolean isInsert(JsonObject configJson, JsonArray dataKey, JsonObject record) throws Exception{
        Connection connection = null;
        Statement statement = null;
        try{
            String query = "select * from cdc_test";

            //where clause
            StringBuilder whereClause = new StringBuilder();
            for(int i=0; i<dataKey.size(); i++){
                String columnName = dataKey.get(i).getAsString();
                String value = record.get(columnName).getAsString();
                if(i < dataKey.size()-1){
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
}
