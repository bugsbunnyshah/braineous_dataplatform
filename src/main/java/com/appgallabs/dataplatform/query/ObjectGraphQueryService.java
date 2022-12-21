package com.appgallabs.dataplatform.query;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.neo4j.driver.*;
import static org.neo4j.driver.Values.parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ApplicationScoped
public class ObjectGraphQueryService {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryService.class);

    @Inject
    private GraphQueryGenerator graphQueryGenerator;

    private Driver driver;

    @ConfigProperty(name = "queryServiceUri")
    private String queryServiceUri;

    @ConfigProperty(name = "queryServiceUser")
    private String queryServiceUser;

    @ConfigProperty(name = "queryServicePassword")
    private String queryServicePassword;

    @PostConstruct
    public void onStart()
    {
        try {
            this.driver = GraphDatabase.driver(this.queryServiceUri,
                    AuthTokens.basic(this.queryServiceUser,this.queryServicePassword));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void onStop(){
        this.driver.close();
    }

    public List<Record> queryByCriteria(String entity, JsonObject criteria)
    {
        String entityLabel = "n1";
        String whereClause = this.graphQueryGenerator.generateWhereClause(entityLabel,criteria);
        String query = "MATCH ("+entityLabel+":"+entity+")\n" +
                whereClause +
                "RETURN "+entityLabel;
        return null;
    }

    public List<Record> navigateByCriteria(String leftEntity,String rightEntity, String relationship, JsonObject criteria, String airport) throws Exception
    {
        try ( Session session = driver.session() )
        {
            List<Record> resultSet = session.writeTransaction(tx ->
            {
                String whereClause = this.graphQueryGenerator.generateWhereClause(leftEntity,criteria);
                String query = "MATCH ("+leftEntity+")--("+rightEntity+")\n" +
                        whereClause+
                        " RETURN airport,flight";

                if(relationship.equals("departure")) {
                    query = "MATCH (f:flight)-[:departure]->(a:airport) WHERE a.name='"+airport+"' RETURN f LIMIT 100";
                }else{
                    query = "MATCH (f:flight)-[:arrival]->(a:airport) WHERE a.name='"+airport+"' RETURN f lIMIT 100";
                }

                System.out.println("************QUERY***************");
                System.out.println(query);
                System.out.println("***************************");

                Result result = tx.run( query);
                return result.list();
            } );
            return resultSet;
        }
    }

    public void saveObjectGraph(String entity,JsonObject json)
    {
        String label = "n1";

        final Map<String, Object> objectMap = JsonFlattener.flattenAsMap(json.toString());
        Set<Map.Entry<String,Object>> entrySet = objectMap.entrySet();
        final Map<String, String> finalMap = new LinkedHashMap<>();
        for(Map.Entry<String,Object> entry:entrySet){
            String key = entry.getKey();
            Object value = entry.getValue();
            if(key != null && value != null) {
                finalMap.put(key, value.toString());
            }
        }

        String query = "CREATE (("+label +":"+entity+" $json)) RETURN "+label;
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run(query,parameters( "json", finalMap));
                return result.list();
            } );
        }
    }

    public void saveObjectRelationship(String entity,JsonObject json)
    {
        logger.info("****SAVE_OBJECT_RELATIONSHIP****");
        String label = "n1";

        final Map<String, Object> objectMap = JsonFlattener.flattenAsMap(json.toString());
        Set<Map.Entry<String,Object>> entrySet = objectMap.entrySet();
        final Map<String, String> finalMap = new LinkedHashMap<>();
        for(Map.Entry<String,Object> entry:entrySet){
            String key = entry.getKey();
            Object value = entry.getValue();
            if(key != null && value != null) {
                finalMap.put(key, value.toString());
            }
        }


        String query = "CREATE (("+label +":"+entity+" $json)) RETURN "+label;
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run(query,parameters( "json", objectMap));
                return result.list();
            } );
        }
    }

    public void establishRelationship(String leftEntity,String rightEntity, String relationship)
    {
        logger.info("****ESTABLISH_RELATIONSHIP****");
        String leftLabel = "n1";
        String rightLabel = "n2";
        String createRelationship = "MATCH\n" +
                "  ("+leftLabel+":"+leftEntity+"),\n" +
                "  ("+rightLabel+":"+rightEntity+")\n" +
                "CREATE ("+leftLabel+")-[r:"+relationship+"]->("+rightLabel+")\n" +
                "RETURN type(r)";
        try ( Session session = driver.session() )
        {
            List<Record> resultData = session.writeTransaction(tx ->
            {
                Result result = tx.run(createRelationship);
                return result.list();
            } );
        }
    }
}
