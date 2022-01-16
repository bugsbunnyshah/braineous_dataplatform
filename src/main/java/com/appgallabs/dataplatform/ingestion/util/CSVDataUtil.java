package com.appgallabs.dataplatform.ingestion.util;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class CSVDataUtil {
    private static Logger logger = LoggerFactory.getLogger(CSVDataUtil.class);

    public static JsonArray convert(String csvData)
    {
        JsonArray array = new JsonArray();

        String[] lines = csvData.split("\n");
        int length = lines.length;
        for (int i = 0; i < length; i++)
        {
            String line = lines[i];
            String[] data = line.split(",");
            JsonObject row = new JsonObject();
            for (int j = 0; j < data.length; j++) {
                String token = data[j];
                String property = "" + j;
                try {
                    Number number = NumberFormat.getInstance().parse(token);
                    row.addProperty(property, number);
                } catch (ParseException e) {
                    row.addProperty(property, token);
                }
            }
            array.add(row);
        }
        return array;
    }

    public static JsonObject convert(JsonArray data)
    {
        //logger.info("*********************************");
        //logger.info("ARRAY: "+data.toString());
        //logger.info("ARRAYSIZE: "+data.size());
        //logger.info("*********************************");

        int rowCount = data.size();
        int columnCount = 0;
        StringBuilder csvBuilder = new StringBuilder();
        Iterator<JsonElement> rows = data.iterator();
        while(rows.hasNext())
        {
            JsonElement row = rows.next();

            if(row.isJsonObject()) {
                JsonObject jsonObject = row.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                columnCount = entrySet.size();
                int count = 0;
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    csvBuilder.append(entry.getValue());
                    if (count != columnCount - 1) {
                        csvBuilder.append(",");
                    }
                    count++;
                }
                csvBuilder.append("\n");
            }
            else
            {
                String value = row.getAsString();
                columnCount = 1;
                csvBuilder.append(value+"\n");
            }
        }

        logger.info("**************************");
        logger.info("RowCount: "+rowCount);
        logger.info("ColumnCount: "+columnCount);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("rows", rowCount);
        jsonObject.addProperty("columns", columnCount);
        jsonObject.addProperty("data", csvBuilder.toString());
        return jsonObject;
    }

    public static Set<String> convertJsonToCsv(JsonArray jsonArray){
        Set<String> csvs = new LinkedHashSet<>();

        StringBuilder csvBuilder = new StringBuilder();
        if(jsonArray == null || jsonArray.size()==0){
            return csvs;
        }

        Map<String, Object> objectMap = JsonFlattener.flattenAsMap(jsonArray.toString());
        Set<String> objectColumns = new LinkedHashSet<>();
        Set<String> arrayFields = new LinkedHashSet<>();
        for(String col:objectMap.keySet()){
            //String[] tokens = col.split("\\.");
            int eraseIndex = col.indexOf("]") + 2;
            String objColumn = col.substring(eraseIndex);
            if(!objColumn.contains("[")) {
                objectColumns.add(objColumn);
            }else{
                arrayFields.add(col);
            }
        }
        csvBuilder.append(CSVDataUtil.getHeader(objectColumns)+"\n");

        List<Row> rows = new ArrayList<>();
        Set<Map.Entry<String,Object>> entries = objectMap.entrySet();
        int currentRowIndex = -1;
        Row currentRow = null;
        for (Map.Entry<String,Object> entry:entries){
            String field = entry.getKey();
            Object value = entry.getValue();

            int start = field.indexOf("[");
            int end = field.indexOf("]");
            int rowIndex = Integer.parseInt(field.substring(start+1,end));
            if(rowIndex != currentRowIndex){
                //Create a new Row
                Row row = new Row();
                rows.add(row);
                currentRow = row;
                currentRowIndex++;
            }

            //Process the current Row
            field = field.substring(end+2);
            //String[] tokens = field.split("\\.");
            if(objectColumns.contains(field)) {
                currentRow.addColumn(field, value);
            }
        }

        for(Row row: rows){
            csvBuilder.append(row.toCsv()+"\n");
        }
        csvs.add(csvBuilder.toString());

        Set<String> arrayCsvs = processArrays(jsonArray,objectMap,arrayFields);
        csvs.addAll(arrayCsvs);

        return csvs;
    }

    private static Set<String> processArrays(JsonArray array,Map<String, Object> objectMap,Set<String> arrayFields){
        Set<String> csvs = new LinkedHashSet<>();
        int currentIndex = 0;
        for (String arrayField : arrayFields) {
            int activeIndex = Integer.parseInt(arrayField.substring(arrayField.indexOf("[") + 1, arrayField.indexOf("]")));
            int parentIndex = arrayField.indexOf(".") + 1;
            int arrayIndex = arrayField.indexOf("[", parentIndex);
            int arrayCounter = Integer.parseInt(arrayField.substring(arrayIndex+1,arrayField.indexOf("]", parentIndex)));
            String object = arrayField.substring(parentIndex,
                    arrayIndex);

            JsonArray children = new JsonArray();
            String[] objectPathTokens = object.split("\\.");
            JsonObject parent = array.get(activeIndex).getAsJsonObject();
            JsonArray arrayToProcess = null;
            for(String pathToken:objectPathTokens){
                if(pathToken.contains("[")){
                    pathToken = pathToken.substring(0,pathToken.indexOf("["));
                }
                JsonElement o = parent.get(pathToken);
                if(o!= null){
                    if(o.isJsonArray()) {
                        arrayToProcess = o.getAsJsonArray();
                        break;
                    }else if(o.isJsonObject()){
                        parent = o.getAsJsonObject();
                    }
                }else{
                    break;
                }
            }
            if(arrayToProcess == null){
                continue;
            }

            children.add(arrayToProcess.get(arrayCounter));
            Set<String> csv = CSVDataUtil.convertJsonToCsv(children);
            csvs.addAll(csv);

            if (activeIndex != currentIndex) {
                currentIndex = activeIndex;
            }
        }
        return csvs;
    }

    private static String getHeader(Set<String> columnNames){
        StringBuilder headerBuilder = new StringBuilder();
        for(String cour: columnNames){
            headerBuilder.append(cour+",");
        }

        String header = headerBuilder.toString();
        String result = header.substring(0, header.length()-1);
        return result;
    }

    private static class Row{
        Map<String,Object> columns;

        private Row(){
            this.columns = new LinkedHashMap<>();
        }

        public void addColumn(String field,Object value){
            columns.put(field,value);
        }

        @Override
        public String toString() {
            return "Row{" +
                    "columns=" + columns +
                    '}';
        }

        public String toCsv(){
            StringBuilder csv = new StringBuilder();
            for(Object column:columns.values()){
                csv.append(column.toString()+",");
            }
            return csv.substring(0,csv.toString().length()-1);
        }
    }
}
