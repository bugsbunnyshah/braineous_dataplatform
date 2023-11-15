package com.appgallabs.dataplatform.infrastructure;

import com.appgallabs.dataplatform.pipeline.Registry;

import com.mongodb.client.MongoClient;

import javax.inject.Singleton;
import java.io.Serializable;

@Singleton
public class RegistryStore implements Serializable {

    public void flushToDb(Tenant tenant, MongoClient mongoClient, Registry registry){
        System.out.println(registry);
    }
}
