package com.appgallabs.dataplatform.infrastructure.security;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

public class InMemoryCallback implements AuthenticationCallback{
    private Map<String,String> profiles;

    public InMemoryCallback(){
        profiles = new HashedMap();
        profiles.put("test@test.com","password");
    }

    @Override
    public boolean authenticate(String tenant,String username, String password) {
        String storedPassword = profiles.get(username);
        if(storedPassword != null && storedPassword.equals(password)){
            return true;
        }

        return false;
    }
}
