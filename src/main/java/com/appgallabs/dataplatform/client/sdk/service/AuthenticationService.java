package com.appgallabs.dataplatform.client.sdk.service;

import com.google.gson.JsonObject;

public class AuthenticationService {
    private static AuthenticationService singleton = new AuthenticationService();

    private AuthenticationService(){

    }

    public static AuthenticationService getInstance(){
        //safe-check, cause why not
        if(AuthenticationService.singleton == null){
            AuthenticationService.singleton = new AuthenticationService();
        }
        return AuthenticationService.singleton;
    }

    public JsonObject authenticate(){
        JsonObject response = new JsonObject();
        return response;
    }
}
