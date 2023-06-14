package com.appgallabs.dataplatform.preprocess;

import com.appgallabs.dataplatform.util.ObjectUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

public class SecurityToken implements Serializable {
    private String principal;
    private String token;
    private String clientId;

    public SecurityToken()
    {

    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public static SecurityToken fromJson(String jsonString)
    {
        SecurityToken securityToken = new SecurityToken();
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        securityToken.principal = jsonObject.get("principal").getAsString();
        //securityToken.principal = ""+ ObjectUtil.hashCode(securityToken.clientId);
        securityToken.token = jsonObject.get("access_token").getAsString();
        return securityToken;
    }

    @Override
    public String toString() {
        return "SecurityToken{" +
                "principal='" + principal + '\'' +
                ", token='" + token + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
