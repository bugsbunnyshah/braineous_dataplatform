package com.appgallabs.dataplatform.client.sdk.api;

import java.io.Serializable;

public class Configuration implements Serializable {
    private int streamSizeInObjects;
    private String ingestionHostUrl;

    private String apiKey;

    private String apiSecret;

    public Configuration apiKey(String apiKey){
        this.apiKey = apiKey;
        return this;
    }

    public Configuration apiSecret(String apiSecret){
        this.apiSecret = apiSecret;
        return this;
    }

    public Configuration streamSizeInObjects(int streamSizeInObjects){
        this.streamSizeInObjects = streamSizeInObjects;
        return this;
    }

    public Configuration ingestionHostUrl(String ingestionHostUrl){
        this.ingestionHostUrl = ingestionHostUrl;

        if(!this.ingestionHostUrl.endsWith("/")){
            this.ingestionHostUrl = this.ingestionHostUrl() + "/";
        }

        return this;
    }

    public int getStreamSizeInObjects(){
        return this.streamSizeInObjects;
    }

    public String ingestionHostUrl(){
        return this.ingestionHostUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }
}
