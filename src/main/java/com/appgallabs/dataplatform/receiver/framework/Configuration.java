package com.appgallabs.dataplatform.receiver.framework;

import java.io.Serializable;

public abstract class Configuration implements Serializable {

    protected String name;

    protected String jsonPathExpression;

    protected String storeDriver;

    public Configuration() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoreDriver() {
        return storeDriver;
    }

    public void setStoreDriver(String storeDriver) {
        this.storeDriver = storeDriver;
    }

    public String getJsonPathExpression() {
        return jsonPathExpression;
    }

    public void setJsonPathExpression(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }
}
