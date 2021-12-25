package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

public interface FetchAgent {
    void startFetch();
    boolean isStarted();
    void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer);
    void setTenant(Tenant tenant);
    void setEntity(String entity);
    void setMapperService(MapperService mapperService);
}
