package com.appgallabs.dataplatform.query;

import org.neo4j.driver.*;

import javax.inject.Singleton;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

@Singleton
public class Neo4JService implements AutoCloseable{
    private final Driver driver;

    public Neo4JService() {
        String uri = "neo4j+s://9c1436ff.databases.neo4j.io:7687";
        String user = "neo4j";
        String password = "oD93a6NKpeIkT8mWt6I09UvZBtL_asBMXq-AXfBWZG8";
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public void createRecord( final String airportName )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( tx ->
            {
                Result result = tx.run( "CREATE (airport:airport) " +
                                "SET airport.name = $airportName " +
                                "RETURN airport.name + ', from node ' + id(airport)",
                        parameters( "airportName", airportName ) );
                return result.single().get( 0 ).asString();
            } );
            System.out.println( greeting );
        }
    }

    public void createAirport( final String airportName, final String airlineName )
    {
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run( "CREATE (AIRPORT:airport {name: $airportName}) -[rel:CONNECTS]->(AIRLINE:airline {name: $airlineName})  ",
                        parameters( "airportName", airportName, "airlineName",airlineName ) );
                return result.list();
            } );
            System.out.println(resultData);
        }
    }

    public void readAirport( final String airportName )
    {
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run( "MATCH (AIRPORT:airport {name: $airportName})-[rel:CONNECTS]->(AIRLINE:airline) "+
                        "RETURN AIRPORT.name,AIRLINE.name",
                        parameters( "airportName", airportName) );
                return result.list();
            } );
            System.out.println(resultData);
        }
    }
}
