package com.appgallabs.dataplatform.ingestion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class CSVDataUtilTests {
    public static Logger logger = LoggerFactory.getLogger(CSVDataUtilTests.class);

    @Test
    public void convertJsonToCsvSimple() throws Exception{
        JsonArray array = new JsonArray();

        for(int i=0; i<2; i++){
            JsonObject json = new JsonObject();
            json.addProperty("col0","blah"+i);
            json.addProperty("col1", "blah"+(i+1));
            array.add(json);
        }

        Set<String> csv = CSVDataUtil.convertJsonToCsv("simple",array);
        System.out.println(csv);
    }

    @Test
    public void convertJsonToCsvWithPrimitiveArray() throws Exception{
        JsonArray array = new JsonArray();
        JsonArray primitiveArray = new JsonArray();
        primitiveArray.add("hello");
        primitiveArray.add("world");
        primitiveArray.add("true");
        for(int i=0; i<1; i++){
            JsonObject json = new JsonObject();
            json.add("pr",primitiveArray);
            array.add(json);
        }
        //System.out.println(primitiveArray);

        //Set<String> csv = CSVDataUtil.convertJsonToCsv("simple",primitiveArray);
        //System.out.println(csv);

        Set<String> csv = CSVDataUtil.convertJsonToCsv("simple",array);
        System.out.println(csv);
    }

    @Test
    public void convertJsonToCsvObject() throws Exception{
        JsonArray array = new JsonArray();

        for(int i=0; i<1; i++){
            JsonArray l0 = this.arrayField();
            JsonArray l1 = this.arrayField();
            JsonArray l2 = this.arrayField();
            JsonArray primitiveArray = new JsonArray();
            primitiveArray.add("hello");
            primitiveArray.add("world");
            /*for(int j=0; j<5; j++){
                primitiveArray.add(j);
            }*/
            JsonObject json = new JsonObject();
            json.add("l0",l0);
            json.addProperty("col0","blah"+i);
            json.addProperty("col1", "blah"+(i+1));
            json.add("object1",this.objectField(l1,l2));
            json.add("object2",this.objectField(l1,l2));
            json.add("primitiveArray", primitiveArray);
            array.add(json);
        }

        Set<String> csv = CSVDataUtil.convertJsonToCsv("objectGraph",array);
        System.out.println(csv);
    }

    @Test
    public void convertJsonToCsvComplex() throws Exception{
        String dataset = IOUtils.toString(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("ingestion/solution1.json"),
                StandardCharsets.UTF_8);
        JsonArray array = JsonParser.parseString(dataset).getAsJsonArray();
        Set<String> csv = CSVDataUtil.convertJsonToCsv("solution",array);
        System.out.println(csv);
    }

    @Test
    public void convertJsonToCsvComplex2() throws Exception{
        String dataset = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/solution2.json"),
                StandardCharsets.UTF_8);
        JsonArray array = JsonParser.parseString(dataset).getAsJsonArray();
        Set<String> csv = CSVDataUtil.convertJsonToCsv("solution",array);
        System.out.println(csv);
    }

    private JsonObject objectField(JsonArray l1,JsonArray l2){
        JsonObject json = new JsonObject();
        json.addProperty("o1","123");
        json.addProperty("o2","1234");
        json.add("l1",l1);
        json.add("nested",this.objectFieldNested(l2));
        return json;
    }

    private JsonObject objectFieldNested(JsonArray l2){
        JsonObject json = new JsonObject();
        json.addProperty("n1","123");
        json.addProperty("n2","1234");
        json.add("l2",l2);
        return json;
    }

    private JsonArray arrayField(){
        JsonArray array = new JsonArray();
        for(int i=0; i<3; i++){
            JsonObject json = new JsonObject();
            json.addProperty("col0","blah"+ UUID.randomUUID());
            json.addProperty("col1", "blah"+UUID.randomUUID());
            array.add(json);
        }
        return array;
    }
}
