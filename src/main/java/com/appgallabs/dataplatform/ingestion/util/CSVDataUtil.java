package com.appgallabs.dataplatform.ingestion.util;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
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

    public static Set<String> convertJsonToCsv(String owner,JsonArray jsonArray){
        Set<String> csvs = new LinkedHashSet<>();

        StringBuilder csvBuilder = new StringBuilder();
        if(jsonArray == null || jsonArray.size()==0){
            return csvs;
        }

        Map<String, Object> objectMap = JsonFlattener.flattenAsMap(jsonArray.toString());
        Set<String> objectColumns = new LinkedHashSet<>();
        Set<String> arrayFields = new LinkedHashSet<>();
        for(String col:objectMap.keySet()){
            int eraseIndex = col.indexOf("]") + 2;
            //String objColumn = col.substring(eraseIndex);
            String objColumn=null;
            try
            {
                objColumn = col.substring(eraseIndex);
            }
            catch (Exception e){
                //This is an array_index
                objColumn = col;
            }

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
            if(field == null || field.trim().length()==0 || !field.contains(".")){
                continue;
            }
            field = field.substring(end + 2);

            //String[] tokens = field.split("\\.");
            if(objectColumns.contains(field)) {
                currentRow.addColumn(field, value);
            }
        }

        for(Row row: rows){
            String csv = row.toCsv();
            if(csv != null && csv.trim().length()!=0) {
                csvBuilder.append(csv + "\n");
            }
        }
        csvs.add(csvBuilder.toString());

        Set<String> arrayCsvs = processArrays(jsonArray,objectMap,arrayFields);
        csvs.addAll(arrayCsvs);


        Set<String> finalCsvs = new LinkedHashSet<>();
        Iterator<String> iterator = csvs.iterator();
        while(iterator.hasNext()){
            String data = iterator.next();
            if(data == null || data.trim().length()==0){
                continue;
            }
            finalCsvs.add(data);
        }

        return finalCsvs;
    }

    public static Set<String> convertJsonToDataSet(String owner,JsonArray jsonArray){
        Set<String> csvs = new LinkedHashSet<>();

        StringBuilder csvBuilder = new StringBuilder();
        if(jsonArray == null || jsonArray.size()==0){
            return csvs;
        }

        Map<String, Object> objectMap = JsonFlattener.flattenAsMap(jsonArray.toString());
        Set<String> objectColumns = new LinkedHashSet<>();
        Set<String> arrayFields = new LinkedHashSet<>();
        for(String col:objectMap.keySet()){
            int eraseIndex = col.indexOf("]") + 2;
            //String objColumn = col.substring(eraseIndex);
            String objColumn=null;
            try
            {
                objColumn = col.substring(eraseIndex);
            }
            catch (Exception e){
                //This is an array_index
                objColumn = col;
            }

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
            value = CSVDataUtil.getDataSetColumnValue(value);

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
            if(field == null || field.trim().length()==0 || !field.contains(".")){
                continue;
            }
            field = field.substring(end + 2);

            //String[] tokens = field.split("\\.");
            if(objectColumns.contains(field)) {
                currentRow.addColumn(field, value);
            }
        }

        for(Row row: rows){
            String csv = row.toCsv();
            if(csv != null && csv.trim().length()!=0) {
                csvBuilder.append(csv + "\n");
            }
        }
        csvs.add(csvBuilder.toString());

        Set<String> arrayCsvs = processArrays(jsonArray,objectMap,arrayFields);
        csvs.addAll(arrayCsvs);


        Set<String> finalCsvs = new LinkedHashSet<>();
        Iterator<String> iterator = csvs.iterator();
        while(iterator.hasNext()){
            String data = iterator.next();
            if(data == null || data.trim().length()==0){
                continue;
            }
            finalCsvs.add(data);
        }

        return finalCsvs;
    }

    public static Set<String> convertJsonToDataSet(String owner, String[] columns,JsonArray jsonArray){
        Set<String> csvs = new LinkedHashSet<>();
        Set<String> columnsToInclude = new LinkedHashSet<>();
        for(String col:columns){
            columnsToInclude.add(col);
        }

        StringBuilder csvBuilder = new StringBuilder();
        if(jsonArray == null || jsonArray.size()==0){
            return csvs;
        }

        Map<String, Object> objectMap = JsonFlattener.flattenAsMap(jsonArray.toString());
        Set<String> objectColumns = new LinkedHashSet<>();
        Set<String> arrayFields = new LinkedHashSet<>();
        for(String col:objectMap.keySet()){
            int eraseIndex = col.indexOf("]") + 2;
            //String objColumn = col.substring(eraseIndex);
            String objColumn=null;
            try
            {
                objColumn = col.substring(eraseIndex);
            }
            catch (Exception e){
                //This is an array_index
                objColumn = col;
            }

            if(!objColumn.contains("[")) {
                if(columnsToInclude.contains(objColumn)) {
                    objectColumns.add(objColumn);
                }
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
            value = CSVDataUtil.getDataSetColumnValue(value);

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
            if(field == null || field.trim().length()==0 || !field.contains(".")){
                continue;
            }
            field = field.substring(end + 2);

            //String[] tokens = field.split("\\.");
            if(objectColumns.contains(field)) {
                currentRow.addColumn(field, value);
            }
        }

        for(Row row: rows){
            String csv = row.toCsv();
            if(csv != null && csv.trim().length()!=0) {
                csvBuilder.append(csv + "\n");
            }
        }
        csvs.add(csvBuilder.toString());

        Set<String> arrayCsvs = processArrays(jsonArray,objectMap,arrayFields);
        csvs.addAll(arrayCsvs);


        Set<String> finalCsvs = new LinkedHashSet<>();
        Iterator<String> iterator = csvs.iterator();
        while(iterator.hasNext()){
            String data = iterator.next();
            if(data == null || data.trim().length()==0){
                continue;
            }
            finalCsvs.add(data);
        }

        return finalCsvs;
    }

    public static Set<String> convertJsonToDataSet(String owner, String[] columns, String[] skipTransform,JsonArray jsonArray){
        Set<String> csvs = new LinkedHashSet<>();
        Set<String> columnsToInclude = new LinkedHashSet<>();
        for(String col:columns){
            columnsToInclude.add(col);
        }
        Set<String> columnsToSkipTransform = new LinkedHashSet<>();
        for(String col:skipTransform){
            columnsToSkipTransform.add(col);
        }

        StringBuilder csvBuilder = new StringBuilder();
        if(jsonArray == null || jsonArray.size()==0){
            return csvs;
        }

        Map<String, Object> objectMap = JsonFlattener.flattenAsMap(jsonArray.toString());
        Set<String> objectColumns = new LinkedHashSet<>();
        Set<String> arrayFields = new LinkedHashSet<>();
        for(String col:objectMap.keySet()){
            int eraseIndex = col.indexOf("]") + 2;
            //String objColumn = col.substring(eraseIndex);
            String objColumn=null;
            try
            {
                objColumn = col.substring(eraseIndex);
            }
            catch (Exception e){
                //This is an array_index
                objColumn = col;
            }

            if(!objColumn.contains("[")) {
                if(columnsToInclude.contains(objColumn)) {
                    objectColumns.add(objColumn);
                }
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
            int indexOf = field.indexOf("]");
            String lookup = field.substring(indexOf+2);
            if(!columnsToSkipTransform.contains(lookup)) {
                value = CSVDataUtil.getDataSetColumnValue(value);
            }

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
            if(field == null || field.trim().length()==0 || !field.contains(".")){
                continue;
            }
            field = field.substring(end + 2);

            //String[] tokens = field.split("\\.");
            if(objectColumns.contains(field)) {
                currentRow.addColumn(field, value);
            }
        }

        for(Row row: rows){
            String csv = row.toCsv();
            if(csv != null && csv.trim().length()!=0) {
                csvBuilder.append(csv + "\n");
            }
        }
        csvs.add(csvBuilder.toString());

        Set<String> arrayCsvs = processArrays(jsonArray,objectMap,arrayFields);
        csvs.addAll(arrayCsvs);


        Set<String> finalCsvs = new LinkedHashSet<>();
        Iterator<String> iterator = csvs.iterator();
        while(iterator.hasNext()){
            String data = iterator.next();
            if(data == null || data.trim().length()==0){
                continue;
            }
            finalCsvs.add(data);
        }

        return finalCsvs;
    }

    private static String getDataSetColumnValue(Object object){
        if(object == null){
            double value = "NaN".hashCode();
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(2000);
            return df.format(value);
        }
        try {
            JsonObject json = new JsonObject();
            json.addProperty("objectHash", object.toString());
            double value = JsonUtil.getJsonHash(json).hashCode();
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(2000);
            return df.format(value);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Set<String> processArrays(JsonArray array,Map<String, Object> objectMap,Set<String> arrayFields){
        Set<String> csvs = new LinkedHashSet<>();
        int currentIndex = 0;
        JsonArray activePrimitiveArray = new JsonArray();
        for (String arrayField : arrayFields) {
            int activeIndex = Integer.parseInt(arrayField.substring(arrayField.indexOf("[") + 1, arrayField.indexOf("]")));
            int parentIndex = arrayField.indexOf(".") + 1;
            int arrayIndex = arrayField.indexOf("[", parentIndex);
            int arrayCounter = Integer.parseInt(arrayField.substring(arrayIndex+1,arrayField.indexOf("]", parentIndex)));
            String object = arrayField.substring(parentIndex,
                    arrayIndex);

            JsonArray children = new JsonArray();
            String[] objectPathTokens = object.split("\\.");

            JsonElement currentObject = array.get(activeIndex);
            JsonObject parent = null;
            if(currentObject.isJsonObject()){
                parent = array.get(activeIndex).getAsJsonObject();
                //process a previous primitive array if active
                if(activePrimitiveArray.size() > 0){
                    String csv = flattenToCsv(activePrimitiveArray);
                    csvs.add(csv);
                    activePrimitiveArray = null;
                }
            }
            else if(currentObject.isJsonPrimitive()){
                //we are at a primitive array index
                activePrimitiveArray.add(currentObject);
                continue;
            }

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

            String owner = objectPathTokens[objectPathTokens.length-1];
            Set<String> csv;
            JsonElement value = arrayToProcess.get(arrayCounter);
            if(value.isJsonPrimitive()){
                String blah = flattenToCsv(arrayToProcess);
                csvs.add(blah);
            }else {
                children.add(arrayToProcess.get(arrayCounter));
                csv = CSVDataUtil.convertJsonToCsv(owner,children);
                csvs.addAll(csv);
            }

            if (activeIndex != currentIndex) {
                currentIndex = activeIndex;
            }
        }
        //process a previous primitive array if active
        if(activePrimitiveArray.size() > 0){
            String csv = flattenToCsv(activePrimitiveArray);
            csvs.add(csv);
        }

        return csvs;
    }

    private static String flattenToCsv(JsonArray primitiveArray){
        StringBuilder csvBuilder = new StringBuilder();
        for(int i=0; i<primitiveArray.size();i++)
        {
            csvBuilder.append("_column_"+UUID.randomUUID()+",");
        }
        String header = csvBuilder.toString();
        int size = header.length();

        StringBuilder finalCsv = new StringBuilder();
        finalCsv.append(header.substring(0,size-1)+"\n");

        StringBuilder dataBuilder = new StringBuilder();
        for(int i=0; i<primitiveArray.size();i++)
        {
            dataBuilder.append(primitiveArray.get(i)+",");
        }

        String data = dataBuilder.toString();
        size = data.length();
        data = data.substring(0,size-1);
        finalCsv.append(data+"\n");

        return finalCsv.toString();
    }

    private static String getHeader(Set<String> columnNames){
        StringBuilder headerBuilder = new StringBuilder();
        for(String cour: columnNames){
            headerBuilder.append(cour+",");
        }

        String header = headerBuilder.toString();
        if(header.trim().length()==0){
            return "";
        }

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
            for (Object column : columns.values()) {
                if(column != null) {
                    csv.append(column + ",");
                }else{
                    csv.append("NaN"+",");
                }
            }

            if(csv.toString().trim().length()==0){
                return null;
            }

            return csv.substring(0, csv.toString().length() - 1);
        }
    }
}
