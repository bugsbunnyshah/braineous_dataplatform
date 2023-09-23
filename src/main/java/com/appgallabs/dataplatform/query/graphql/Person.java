package com.appgallabs.dataplatform.query.graphql;

public class Person {

    String name;
    Country country;

    public Person() {
    }

    public Person(String name, Country country) {
        this.name = name;
        this.country = country;
    }

    // Getters/Setters omitted for brevity

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
