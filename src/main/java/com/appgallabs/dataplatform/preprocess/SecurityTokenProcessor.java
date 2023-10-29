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
        String tenant = context.getHeaderString("tenant");
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

            logger.info("*****************************************************************");
            logger.info("(SecurityTokenContainer): " + this.securityTokenContainer);
            logger.info("(SecurityToken): " + this.securityTokenContainer.getSecurityToken());
            logger.info("(Principal): " + this.securityTokenContainer.getSecurityToken().getPrincipal());
            logger.info("(ClientId): " + tenant);
            logger.info("*****************************************************************");
        }else{
            Response.Status status = Response.Status.UNAUTHORIZED;
            Response response = Response.status(status).build();
            context.abortWith(response);
        }
    }
}
