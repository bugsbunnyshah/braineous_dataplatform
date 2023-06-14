package com.appgallabs.dataplatform.infrastructure.security;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/data/security")
public class JWTTokenManager {
    private static Logger logger  = LoggerFactory.getLogger(JWTTokenManager.class);

    @Inject
    private JWTTokenManagerService jwtTokenManagerService;

    @POST
    @Path("issue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response issueToken(@RequestBody String inputJson) throws Exception{
        try {
            JsonObject input = JsonParser.parseString(inputJson).getAsJsonObject();

            String tenant = input.get("tenant").getAsString();
            String username = input.get("username").getAsString();
            String password = input.get("password").getAsString();

            String token = this.jwtTokenManagerService.issueToken(tenant,username,password);

            JsonObject json = new JsonObject();
            json.addProperty("token",token);
            return Response.ok(json.toString()).build();
        }catch(AuthenticatedFailed authenticatedFailed){
            JsonObject error = new JsonObject();
            error.addProperty("message", "authentication_failed");
            return Response.status(401).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}
