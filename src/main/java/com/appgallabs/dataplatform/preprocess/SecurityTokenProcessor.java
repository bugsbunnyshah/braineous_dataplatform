package com.appgallabs.dataplatform.preprocess;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
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
        //System.out.println("*********TOKEN_FILTER_ACTIVE***************");
        String authorization = context.getHeaderString("Authorization");
        if(authorization != null)
        {
            String[] array = authorization.split(" ");
            String bearerToken = array[1];
            System.out.println(bearerToken);
            String principal = context.getHeaderString("Principal");
            JsonObject json = new JsonObject();
            json.addProperty("access_token", bearerToken);
            json.addProperty("principal", principal);
            SecurityToken securityToken = SecurityToken.fromJson(json.toString());
            this.securityTokenContainer.setSecurityToken(securityToken);

            logger.info("*****************************************************************");
            logger.info("(SecurityTokenContainer): " + this.securityTokenContainer);
            logger.info("(SecurityToken): " + this.securityTokenContainer.getSecurityToken());
            logger.info("(Principal): " + this.securityTokenContainer.getSecurityToken().getPrincipal());
            logger.info("(ClientId): " + principal);
            logger.info("*****************************************************************");
        }
    }
}
