package com.appgallabs.dataplatform.ingestion.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named("mock")
public class MockDBDriver implements Driver{
    @Override
    public String name() {
        return "mock";
    }
}
