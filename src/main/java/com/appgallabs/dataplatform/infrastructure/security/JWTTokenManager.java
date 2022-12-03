package com.appgallabs.dataplatform.infrastructure.security;

import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.Claims;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;

@Path("/data/security")
public class JWTTokenManager {

    @GET
    @Path("issue")
    @Produces(MediaType.TEXT_PLAIN)
    public String issueToken(){
        String token =
                Jwt.issuer("https://braineous.appgallabs.io/issuer")
                        .upn("jdoe@quarkus.io")
                        .groups(new HashSet<>(Arrays.asList("User", "Admin")))
                        .claim(Claims.birthdate.name(), "2001-07-13")
                        .sign();
        return token;
    }
}
