package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;

public class Tenant implements Serializable {
    private String principal;
    private String apiSecret;

    private String name;

    private String email;

    private String password;

    public Tenant() {
    }

    public Tenant(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "principal='" + principal + '\'' +
                '}';
    }

    public JsonObject toJson(){
        Gson gson = JsonUtil.getGson();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        jsonObject.remove("apiSecret");
        jsonObject.addProperty("apiKey", this.principal);
        return jsonObject;
    }

    public JsonObject toJsonForStore(){
        Gson gson = JsonUtil.getGson();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        jsonObject.addProperty("apiKey", this.principal);
        return jsonObject;
    }

    public static Tenant parse(String jsonString){
        Gson gson = JsonUtil.getGson();

        Tenant parsed = gson.fromJson(jsonString,Tenant.class);

        return parsed;
    }
}
