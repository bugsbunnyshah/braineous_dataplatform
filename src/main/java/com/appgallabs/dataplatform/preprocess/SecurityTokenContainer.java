package com.appgallabs.dataplatform.preprocess;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class SecurityTokenContainer implements Serializable
{
    private static Logger logger = LoggerFactory.getLogger(SecurityTokenContainer.class);

    private static ThreadLocal<SecurityToken> tokenContainer;

    public SecurityTokenContainer()
    {
        tokenContainer = new ThreadLocal<>();
    }

    public void setSecurityToken(SecurityToken securityToken)
    {
        tokenContainer.set(securityToken);
    }

    public SecurityToken getSecurityToken()
    {
        return tokenContainer.get();
    }

    public Tenant getTenant()
    {
        Tenant tenant = new Tenant();
        tenant.setPrincipal(this.getSecurityToken().getPrincipal());
        return tenant;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("tenant",this.getTenant().toString());
        return jsonObject.toString();
    }
}
