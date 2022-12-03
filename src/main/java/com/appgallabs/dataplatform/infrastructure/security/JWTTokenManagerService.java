package com.appgallabs.dataplatform.infrastructure.security;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.smallrye.jwt.build.Jwt;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Singleton
public class JWTTokenManagerService {
    private static Logger logger  = LoggerFactory.getLogger(JWTTokenManagerService.class);

    Map<String,AuthenticationCallback> callbackMap = new HashMap<>();

    @ConfigProperty(name = "environment")
    private String environment;

    @PostConstruct
    public void start(){
        try {
            //load callbacks
            String configFile = "authenticationCallbacks_"+environment+".json";
            String configJsonString = IOUtils.toString(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(
                            configFile),
                    StandardCharsets.UTF_8
            );
            JsonArray configJson = JsonParser.parseString(configJsonString).getAsJsonArray();
            Iterator<JsonElement> iterator = configJson.iterator();
            while (iterator.hasNext()) {
                JsonObject entityConfigJson = iterator.next().getAsJsonObject();
                String tenant = entityConfigJson.get("tenant").getAsString();
                String callback = entityConfigJson.get("callback").getAsString();
                AuthenticationCallback authenticationCallback = (AuthenticationCallback) Thread.currentThread().getContextClassLoader()
                        .loadClass(callback).newInstance();
                this.callbackMap.put(tenant,authenticationCallback);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public String issueToken(String tenant,String username,String password) throws AuthenticatedFailed{
        AuthenticationCallback authenticationCallback = this.callbackMap.get(tenant);

        boolean success = authenticationCallback.authenticate(tenant, username, password);
        if(!success){
            throw new AuthenticatedFailed();
        }

        String token =
                Jwt.issuer("https://braineous.appgallabs.io/issuer")
                        .upn(username)
                        .groups(new HashSet<>(Arrays.asList("User", "Admin")))
                        .claim(Claims.birthdate.name(), "2001-07-13")
                        .sign();
        return token;
    }
}
