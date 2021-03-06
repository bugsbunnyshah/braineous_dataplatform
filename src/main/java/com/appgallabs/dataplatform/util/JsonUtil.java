package com.appgallabs.dataplatform.util;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void print(JsonElement jsonElement)
    {
        if(jsonElement.isJsonArray())
        {
            logger.info("******ARRAY_SIZE: "+jsonElement.getAsJsonArray().size()+"**********");
        }
        logger.info(gson.toJson(jsonElement));
    }

    public static void print(Class caller,JsonElement jsonElement)
    {
        logger.info("*****JSONUtil*********************");
        logger.info("CALLER: "+caller.toString());
        if(jsonElement.isJsonArray())
        {
            logger.info("******ARRAY_SIZE: "+jsonElement.getAsJsonArray().size()+"**********");
        }
        logger.info(gson.toJson(jsonElement));
    }

    public static void printStdOut(JsonElement jsonElement)
    {
        if(jsonElement.isJsonArray())
        {
            System.out.println("******ARRAY_SIZE: "+jsonElement.getAsJsonArray().size()+"**********");
        }
        System.out.println(gson.toJson(jsonElement));
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

    private static String hash(String original) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        md5.update(original.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md5.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return myHash;
    }
}
