package com.appgallabs.dataplatform.query;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.neo4j.driver.*;
import static org.neo4j.driver.Values.parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class ObjectGraphQueryService {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryService.class);

    @Inject
    private GraphQueryGenerator graphQueryGenerator;

    private Driver driver;

    @PostConstruct
    public void onStart()
    {
        String uri = "neo4j+s://9c1436ff.databases.neo4j.io:7687";
        String user = "neo4j";
        String password = "oD93a6NKpeIkT8mWt6I09UvZBtL_asBMXq-AXfBWZG8";
        this.driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @PreDestroy
    public void onStop(){
        this.driver.close();
    }

    public JsonArray queryByCriteria(String entity, JsonObject criteria)
    {
        JsonArray response = new JsonArray();
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                String entityLabel = "n1";
                String whereClause = this.graphQueryGenerator.generateWhereClause(entityLabel,criteria);
                System.out.println(whereClause);
                String query = "MATCH ("+entityLabel+":"+entity+")\n" +
                        //"WHERE a.name='Dallas'\n" +
                        "RETURN "+entityLabel;
                System.out.println(query);
                Result result = tx.run( query);
                return result.list();
            } );
            System.out.println(resultData);
        }
        return response;
    }

    public JsonArray navigateByCriteria(String leftEntity,String rightEntity, String relationship, JsonObject criteria) throws Exception
    {
        JsonArray response = new JsonArray();
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                String leftLabel = "n1";
                String rightLabel = "n2";
                String whereClause = this.graphQueryGenerator.generateWhereClause(leftLabel,criteria);
                System.out.println(whereClause);
                String query = "MATCH ("+leftLabel+":"+leftEntity+")-[r:"+relationship+"]->("+rightLabel+":"+rightEntity+")\n" +
                        //"WHERE a.name='Dallas'\n" +
                        "RETURN "+leftLabel+","+rightLabel;
                System.out.println(query);
                Result result = tx.run( query);
                return result.list();
            } );
        }
        return response;
    }

    public void saveObjectGraph(String label,String entity,JsonObject json)
    {
        //TODO:FIME: support all data types
        String query = "CREATE (("+label +":"+entity+" $json)) RETURN "+label;
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run(query,parameters( "json", JsonFlattener.flattenAsMap(json.toString())
                ));
                return result.list();
            } );
        }
    }

    public void establishRelationship(String leftEntity,String rightEntity)
    {
        String getNodesQuery = "MATCH\n" +
                "  (a:airline_network_airport),\n" +
                "  (f:airline_network_flight)\n" +
                "CREATE (a)-[r:foobar]->(f)\n" +
                "RETURN type(r)";

        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run(getNodesQuery);
                return result.list();
            } );
        }
    }
}
