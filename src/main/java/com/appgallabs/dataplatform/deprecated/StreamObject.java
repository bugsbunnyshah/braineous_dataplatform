package com.appgallabs.dataplatform.deprecated;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class StreamObject implements Serializable {
    private String entity;
    private String principal;
    private String data;
    private String dataLakeId;
    private String chainId;

    private int batchSize;

    public StreamObject() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getDataLakeId() {
        return dataLakeId;
    }

    public void setDataLakeId(String dataLakeId) {
        this.dataLakeId = dataLakeId;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();

        if(this.entity != null) {
            json.addProperty("entity", this.entity);
        }

        if(this.principal != null){
            json.addProperty("principal",this.principal);
        }

        if(this.data != null){
            json.addProperty("data",this.data);
        }

        if(this.dataLakeId != null){
            json.addProperty("dataLakeId",this.dataLakeId);
        }

        if(this.chainId != null){
            json.addProperty("chainId",this.chainId);
        }

        json.addProperty("batchSize",this.batchSize);

        return json;
    }
}
