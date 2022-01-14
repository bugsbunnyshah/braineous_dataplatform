package com.appgallabs.dataplatform.ingestion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVDataUtilTests {
    public static Logger logger = LoggerFactory.getLogger(CSVDataUtilTests.class);

    @Test
    public void convertJsonToCsv() throws Exception{
        JsonArray array = new JsonArray();

        for(int i=0; i<2; i++){
            JsonObject json = new JsonObject();
            json.addProperty("col0","blah"+i);
            json.addProperty("col1", "blah"+(i+1));
            array.add(json);
        }

        String csv = CSVDataUtil.convertJsonToCsv(array);
        System.out.println(csv);
    }
}
