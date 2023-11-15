package com.appgallabs.dataplatform.client.sdk.api;

import java.io.Serializable;

public class Configuration implements Serializable {
    private int streamSizeInBytes;
    private String ingestionHostUrl;

    public Configuration streamSizeInBytes(int streamSizeInBytes){
        this.streamSizeInBytes = streamSizeInBytes;
        return this;
    }

    public int streamSizeInBytes(){
        return this.streamSizeInBytes;
    }

    public Configuration ingestionHostUrl(String ingestionHostUrl){
        this.ingestionHostUrl = ingestionHostUrl;

        if(!this.ingestionHostUrl.endsWith("/")){
            this.ingestionHostUrl = this.ingestionHostUrl() + "/";
        }

        return this;
    }

    public String ingestionHostUrl(){
        return this.ingestionHostUrl;
    }
}
