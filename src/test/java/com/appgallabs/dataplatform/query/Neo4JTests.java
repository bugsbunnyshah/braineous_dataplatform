package com.appgallabs.dataplatform.query;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neo4JTests {
    private static Logger logger = LoggerFactory.getLogger(GraphQueryGenerator.class);

    @Test
    public void createAirportAirlineRelationship() throws Exception{
        Neo4JService service = new Neo4JService();
        service.createAirport("Dallas International", "Delta Airlines");
        service.readAirport("Dallas International");
    }
}
