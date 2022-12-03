package com.appgallabs.dataplatform.infrastructure.security;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.smallrye.jwt.build.Jwt;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.jwt.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Path("/data/security")
public class JWTTokenManager {
    private static Logger logger  = LoggerFactory.getLogger(JWTTokenManager.class);

    Map<String,String> callbackMap = new HashMap<>();


    @PostConstruct
    public void start(){
        try {
            //TODO: Environment
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response issueToken(@RequestBody String inputJson) throws Exception{
        try {
            JsonObject input = JsonParser.parseString(inputJson).getAsJsonObject();

            String tenant = input.get("tenant").getAsString();
            String username = input.get("username").getAsString();
            String password = input.get("password").getAsString();

            String callBackClass = this.callbackMap.get(tenant);
            AuthenticationCallback authenticationCallback = (AuthenticationCallback) Thread.currentThread().getContextClassLoader()
                    .loadClass(callBackClass).newInstance();
            boolean success = authenticationCallback.authenticate(tenant, username, password);
            if(!success){
                JsonObject error = new JsonObject();
                error.addProperty("message", "authentication_failed");
                return Response.status(401).entity(error.toString()).build();
            }

            String token =
                    Jwt.issuer("https://braineous.appgallabs.io/issuer")
                            .upn(username)
                            .groups(new HashSet<>(Arrays.asList("User", "Admin")))
                            .claim(Claims.birthdate.name(), "2001-07-13")
                            .sign();
            JsonObject json = new JsonObject();
            json.addProperty("token",token);
            return Response.ok(json.toString()).build();
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}
