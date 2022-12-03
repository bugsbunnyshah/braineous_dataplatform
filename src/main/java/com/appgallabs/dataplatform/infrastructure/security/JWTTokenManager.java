package com.appgallabs.dataplatform.infrastructure.security;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.smallrye.jwt.build.Jwt;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.jwt.Claims;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Path("/data/security")
public class JWTTokenManager {

    Map<String,String> callbackMap = new HashMap<>();


    @PostConstruct
    public void start(){
        try {
            String environment = "dev";

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
                this.callbackMap.put(tenant,callback);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("issue")
    @Produces(MediaType.TEXT_PLAIN)
    public String issueToken(@RequestBody String inputJson) throws Exception{
        JsonObject input = JsonParser.parseString(inputJson).getAsJsonObject();
        JsonUtil.printStdOut(input);

        String tenant = input.get("tenant").getAsString();
        String username = input.get("username").getAsString();
        String password = input.get("password").getAsString();

        String callBackClass = this.callbackMap.get(tenant);
        System.out.println(callBackClass);
        AuthenticationCallback authenticationCallback = (AuthenticationCallback) Thread.currentThread().getContextClassLoader()
                .loadClass(callBackClass).newInstance();
        boolean success = authenticationCallback.authenticate(tenant,username,password);
        System.out.println("SUCCESS: "+success);

        //TODO: authenticate using callback

        String token =
                Jwt.issuer("https://braineous.appgallabs.io/issuer")
                        .upn(username)
                        .groups(new HashSet<>(Arrays.asList("User", "Admin")))
                        .claim(Claims.birthdate.name(), "2001-07-13")
                        .sign();
        return token;
    }
}
