package com.appgallabs.dataplatform.preprocess;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Priority(0)
@Provider
public class SecurityTokenProcessor implements ContainerRequestFilter
{
    private static Logger logger = LoggerFactory.getLogger(SecurityTokenProcessor.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;


    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        /*String tenant = context.getHeaderString("tenant");
        if(tenant != null)
        {
            String token = context.getHeaderString("token");
            String[] array = token.split(" ");
            String bearerToken = array[1];
            JsonObject json = new JsonObject();
            json.addProperty("access_token", bearerToken);
            json.addProperty("principal", tenant);
            SecurityToken securityToken = SecurityToken.fromJson(json.toString());
            this.securityTokenContainer.setSecurityToken(securityToken);


            //logger.info("*****************************************************************");
            //logger.info("(SecurityTokenContainer): " + this.securityTokenContainer);
            //logger.info("(SecurityToken): " + this.securityTokenContainer.getSecurityToken());
            //logger.info("(Principal): " + this.securityTokenContainer.getSecurityToken().getPrincipal());
            //logger.info("(ClientId): " + tenant);
            //logger.info("*****************************************************************");

        }else{
            Response.Status status = Response.Status.UNAUTHORIZED;
            Response response = Response.status(status).build();
            context.abortWith(response);
        }*/
        String bearerToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlNlV1JQRlJraWZSNWpwMndOSFliSCJ9.eyJpc3MiOiJodHRwczovL2FwcGdhbGxhYnMudXMuYXV0aDAuY29tLyIsInN1YiI6IlBBbERla0FvbzBYV2pBaWNVOVNRREtneTdCMHkycDJ0QGNsaWVudHMiLCJhdWQiOiJodHRwczovL2FwcGdhbGxhYnMudXMuYXV0aDAuY29tL2FwaS92Mi8iLCJpYXQiOjE2MDEzMjY5MzIsImV4cCI6MTYwMTQxMzMzMiwiYXpwIjoiUEFsRGVrQW9vMFhXakFpY1U5U1FES2d5N0IweTJwMnQiLCJzY29wZSI6InJlYWQ6Y2xpZW50X2dyYW50cyBjcmVhdGU6Y2xpZW50X2dyYW50cyBkZWxldGU6Y2xpZW50X2dyYW50cyIsImd0eSI6ImNsaWVudC1jcmVkZW50aWFscyJ9.eGSMpm1P2rSFFP6s0x4_csKrDSSd8PTko-hHyETSILt9bB6Q0y7u8ky6yOl1piG9RBd5LW7Hy_R_T0bWJjRGEmZ7yUYkaIKafw4oTDpvPOC9T6CeJHcYgaCuroOMSFQhmk9LfZflnl4ODCt21yr4WrI8Teeh8YK5jZ6o8gsk-XrtKISEp04c0GHzBzaMvd5dmBCzSAhFifq3IzvJKkSL5WdXaAsdPgP_BVm5vTYMwTPEm05Cd6E-5S3pPykLO7APKk8s1kLeXSvXnAPkX6y1pbCfZz7dUvHz-fLgMMhx2PUkS_8wM3N2wSZ7rni6MZ3TM7kmqgQy_9SPtOnWSuZfZg";
        String tenant = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
        JsonObject json = new JsonObject();
        json.addProperty("access_token", bearerToken);
        json.addProperty("principal", tenant);
        SecurityToken securityToken = SecurityToken.fromJson(json.toString());
        this.securityTokenContainer.setSecurityToken(securityToken);
    }
}
