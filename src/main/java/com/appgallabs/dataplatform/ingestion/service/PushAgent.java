package com.appgallabs.dataplatform.ingestion.service;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;

public interface PushAgent {
    void receiveData(JsonArray data);
    void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer);
    void setTenant(Tenant tenant);
    void setEntity(String entity);
    void setMapperService(MapperService mapperService);
}
