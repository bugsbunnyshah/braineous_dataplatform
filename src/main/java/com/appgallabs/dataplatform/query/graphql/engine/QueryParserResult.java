package com.appgallabs.dataplatform.query.graphql.engine;

import java.util.List;
import java.util.Map;

public class QueryParserResult {
    private String entity;

    private List<String> projectionFields;

    private Map<String,String> criteria;

    public QueryParserResult() {
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public List<String> getProjectionFields() {
        return projectionFields;
    }

    public void setProjectionFields(List<String> projectionFields) {
        this.projectionFields = projectionFields;
    }

    public Map<String, String> getCriteria() {
        return criteria;
    }

    public void setCriteria(Map<String, String> criteria) {
        this.criteria = criteria;
    }

    @Override
    public String toString() {
        return "QueryParserResult{" +
                "entity='" + entity + '\'' +
                ", projectionFields=" + projectionFields +
                ", criteria=" + criteria +
                '}';
    }
}
