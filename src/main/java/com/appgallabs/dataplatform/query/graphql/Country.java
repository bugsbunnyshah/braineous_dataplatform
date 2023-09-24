package com.appgallabs.dataplatform.query.graphql;

public class Country {
    private String name;
    private String symbol;

    public Country() {
    }

    public Country(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    // Getters/Setters omitted for brevity

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
