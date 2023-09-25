package com.appgallabs.dataplatform.query.graphql;

public class Document {

    private String dataLakeId;

    private String data;

    public Document() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataLakeId() {
        return dataLakeId;
    }

    public void setDataLakeId(String dataLakeId) {
        this.dataLakeId = dataLakeId;
    }
}
