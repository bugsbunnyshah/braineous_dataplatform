package com.appgallabs.dataplatform.ingestion.util;

public class JobManagerUtil {

    public static String getCatalog(String apiKey, String pipeId){
        String catalog = apiKey.replaceAll("-", "").toLowerCase();
        return catalog;
    }

    public static String getDatabase(String apiKey, String pipeId){
        String database = pipeId.replaceAll("-", "").toLowerCase();
        return database;
    }

    public static String getTableName(String entity){
        String tableName = entity.replaceAll("-", "").toLowerCase();
        return tableName;
    }

    public static String getTable(String apiKey, String pipeId, String entity){
        String catalog = JobManagerUtil.getCatalog(apiKey, pipeId);
        String database = JobManagerUtil.getDatabase(apiKey, pipeId);
        String tableName = entity.replaceAll("-", "").toLowerCase();
        String table = catalog + "." + database + "." + tableName;
        return table;
    }
}
