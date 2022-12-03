package com.appgallabs.dataplatform.infrastructure.security;

public interface AuthenticationCallback {
    boolean authenticate(String tenant,String username,String password);
}
