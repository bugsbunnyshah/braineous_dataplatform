package com.appgallabs.dataplatform.infrastructure.security;

public class InMemoryCallback implements AuthenticationCallback{
    @Override
    public boolean authenticate(String tenant,String username, String password) {
        return true;
    }
}
