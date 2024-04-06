package com.appgallabs.dataplatform.ingestion.util;

import org.apache.commons.lang3.StringUtils;

public class JobManagerUtil {

    public static String getCatalog(String apiKey, String pipeId){
        String catalog = JobManagerUtil.generateAlphabetString(apiKey);
        return catalog;
    }

    public static String getDatabase(String apiKey, String pipeId){
        String database = JobManagerUtil.generateAlphabetString(pipeId);
        return database;
    }

    public static String getTableName(String entity){
        String tableName = JobManagerUtil.generateAlphabetString(entity);
        return tableName;
    }

    public static String getTable(String apiKey, String pipeId, String entity){
        String catalog = JobManagerUtil.getCatalog(apiKey, pipeId);
        String database = JobManagerUtil.getDatabase(apiKey, pipeId);
        String tableName = JobManagerUtil.getTableName(entity);
        String table = catalog + "." + database + "." + tableName;
        return table;
    }

    public static String generateAlphabetString(String input){
        StringBuilder alphabetBuilder = new StringBuilder();

        //TODO: optimize (GA)
        char[] characters = input.toCharArray();
        for(char character:characters){
            String oneCharacterString = String.valueOf(character);

            boolean isAlpha = StringUtils.isAlpha(oneCharacterString);
            if(!isAlpha){
                oneCharacterString = "a";
            }

            alphabetBuilder.append(oneCharacterString);
        }

        return alphabetBuilder.toString();
    }
}
