package com.appgallabs.dataplatform.util;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.*;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }

    public static void print(JsonElement jsonElement)
    {
        if(jsonElement.isJsonArray())
        {
            logger.info("******ARRAY_SIZE: "+jsonElement.getAsJsonArray().size()+"**********");
        }
        logger.info(gson.toJson(jsonElement));
    }

    public static String getJsonHash(JsonObject jsonObject) throws NoSuchAlgorithmException {
        Map<String, Object> jsonMap = JsonFlattener.flattenAsMap(jsonObject.toString());
        Map<String,Object> sortedMap = new TreeMap<>();
        Set<Map.Entry<String,Object>> entrySet = jsonMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            sortedMap.put(entry.getKey(),entry.getValue());
        }
        String jsonHashString = sortedMap.toString();
        return JsonUtil.hash(jsonHashString);
    }

    public static String getJsonHash(JsonArray jsonArray) throws NoSuchAlgorithmException {
        Map<String, Object> jsonMap = JsonFlattener.flattenAsMap(jsonArray.toString());
        Map<String,Object> sortedMap = new TreeMap<>();
        Set<Map.Entry<String,Object>> entrySet = jsonMap.entrySet();
        for(Map.Entry<String,Object> entry:entrySet){
            sortedMap.put(entry.getKey(),entry.getValue());
        }
        String jsonHashString = sortedMap.toString();
        return JsonUtil.hash(jsonHashString);
    }

    private static String hash(String original) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        md5.update(original.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md5.digest();
        String myHash = Hex.encodeHexString(digest).toUpperCase();
        return myHash;
    }

    public static JsonElement validateJson(String jsonString){
        try{
            return JsonParser.parseString(jsonString);
        }catch (Exception e){
            return null;
        }
    }

    public static void printStdOut(JsonElement jsonElement)
    {
        if(jsonElement.isJsonArray())
        {
            System.out.println("******ARRAY_SIZE: "+jsonElement.getAsJsonArray().size()+"**********");
        }
        System.out.println(gson.toJson(jsonElement));
        System.out.println("**********************");
    }

    public static boolean isCsvValid(String csvData){
        int numberOfColumns = 0;
        String[] lines = csvData.split("\n");
        int length = lines.length;
        for (int i = 0; i < length; i++) {
            String line = lines[i];
            String[] data = line.split(",");
            if(data.length == 1){
                return false;
            }

            if(numberOfColumns == 0){
                numberOfColumns = data.length;
                continue;
            }

            if(data.length != numberOfColumns){
                return false;
            }
        }
        return true;
    }

    public static boolean isJsonValid(String jsonData){
        JsonElement json = JsonUtil.validateJson(jsonData);
        if(json == null){
            return false;
        }
        return true;
    }

    public static boolean isXmlValid(String xmlData){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            try {
                builder.parse(new InputSource(new StringReader(xmlData)));
            }catch(Exception e){
                return false;
            }

            return true;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
