package com.appgallabs.dataplatform.infrastructure;

import java.io.Serializable;

public class Tenant implements Serializable {
    private String principal;

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "principal='" + principal + '\'' +
                '}';
    }
}
