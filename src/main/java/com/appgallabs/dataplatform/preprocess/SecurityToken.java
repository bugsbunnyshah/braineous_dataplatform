package com.appgallabs.dataplatform.preprocess;

import com.appgallabs.dataplatform.util.ObjectUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

public class SecurityToken implements Serializable {
    private String principal;
    private String token;

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

    public static SecurityToken fromJson(String jsonString)
    {
        SecurityToken securityToken = new SecurityToken();
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        securityToken.principal = jsonObject.get("principal").getAsString();
        //securityToken.principal = ""+ ObjectUtil.hashCode(securityToken.clientId);

        if(jsonObject.has("access_token")) {
            securityToken.token = jsonObject.get("access_token").getAsString();
        }

        if(jsonObject.has("token")) {
            securityToken.token = jsonObject.get("token").getAsString();
        }

        return securityToken;
    }

    @Override
    public String toString() {
        return "{" +
                "principal='" + principal + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
