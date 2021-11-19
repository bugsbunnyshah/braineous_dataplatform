package com.appgallabs.dataplatform.preprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class AITrafficContainer
{
    private static Logger logger = LoggerFactory.getLogger(AITrafficContainer.class);

    private static ThreadLocal<String> tokenContainer;

    public AITrafficContainer()
    {
        tokenContainer = new ThreadLocal<>();
    }

    public void setChainId(String chainId)
    {
        tokenContainer.set(chainId);
    }

    public String getChainId()
    {
        return tokenContainer.get();
    }
}
