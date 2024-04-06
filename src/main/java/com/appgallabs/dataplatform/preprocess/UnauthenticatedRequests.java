package com.appgallabs.dataplatform.preprocess;

public class UnauthenticatedRequests {

    public static boolean isAllowed(String requestUri){
        if(
                requestUri.contains("authenticate_tenant") ||
                requestUri.contains("create_tenant")
        ){
            return true;
        }
        return false;
    }
}
