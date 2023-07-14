package com.appgallabs.dataplatform.ingestion.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named("mongodb")
public class MongoDBDriver implements Driver{
    @Override
    public String name() {
        return "mongodb";
    }
}
