package com.appgallabs.dataplatform.preprocess;

import com.appgallabs.dataplatform.infrastructure.security.ApiKeyManager;
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

    @Inject
    private ApiKeyManager apiKeyManager;


    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        String apiKey = context.getHeaderString("x-api-key");
        String apiKeySecret = context.getHeaderString("x-api-key-secret");
        if(apiKey != null && apiKeySecret != null)
        {
            SecurityToken securityToken = new SecurityToken();
            securityToken.setPrincipal(apiKey);
            securityToken.setToken(apiKeySecret);
            this.securityTokenContainer.setSecurityToken(securityToken);

            boolean success = this.apiKeyManager.authenticate(apiKey, apiKeySecret);

            if(success) {
                logger.info("*************************SERVER_COMPONENT****************************************");
                logger.info("(SecurityTokenContainer): " + this.securityTokenContainer);
                logger.info("(Principal): " + this.securityTokenContainer.getSecurityToken().getPrincipal());
                logger.info("(Token): " + this.securityTokenContainer.getSecurityToken().getToken());
                logger.info("*****************************************************************");
            }else{
                this.unauthorized(context);
            }

        }else{
            this.unauthorized(context);
        }
    }

    private void unauthorized(ContainerRequestContext context){
        Response.Status status = Response.Status.UNAUTHORIZED;
        Response response = Response.status(status).build();
        context.abortWith(response);
    }
}
