package com.appgallabs.dataplatform.infrastructure.security;

import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.Claims;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;

@Singleton
public class JWTTokenManager {

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
