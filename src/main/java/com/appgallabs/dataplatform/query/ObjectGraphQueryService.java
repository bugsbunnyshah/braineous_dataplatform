package com.appgallabs.dataplatform.query;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
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

    private Map<String,EntityCallback> callbackMap = new HashMap<>();

    @PostConstruct
    public void onStart()
    {
        try {
            //staging
            //String uri = "neo4j+s://99010aff.databases.neo4j.io:7687";
            //String user = "neo4j";
            //String password = "AM8_XkkyVerBSDt9nsJ8-3dEEd0aIgw2wO-yLAf2pZU";


            //production
            String uri = "neo4j+s://f68f3f83.databases.neo4j.io:7687";
            String user = "neo4j";
            String password = "XqFdNRKumxztTfMID57cDiNvb5fba-tR_hiNTazSqNg";


            this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

            //load callbacks
            String configJsonString = IOUtils.toString(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("entityCallbacks.json"),
                    StandardCharsets.UTF_8
            );
            JsonArray configJson = JsonParser.parseString(configJsonString).getAsJsonArray();

            //TODO
            /*Iterator<JsonElement> iterator = configJson.iterator();
            while (iterator.hasNext()) {
                JsonObject entityConfigJson = iterator.next().getAsJsonObject();
                String entity = entityConfigJson.get("entity").getAsString();
                String callback = entityConfigJson.get("callback").getAsString();
                EntityCallback object = (EntityCallback) Thread.currentThread().getContextClassLoader().
                        loadClass(callback).getDeclaredConstructor().newInstance();
                this.callbackMap.put(entity, object);
            }*/
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
        /*try ( Session session = driver.session() )
        {
            List<Record> resultSet = session.writeTransaction(tx ->
            {
                String entityLabel = "n1";
                String whereClause = this.graphQueryGenerator.generateWhereClause(entityLabel,criteria);
                String query = "MATCH ("+entityLabel+":"+entity+")\n" +
                        whereClause +
                        "RETURN "+entityLabel;
                System.out.println(query);
                Result result = tx.run( query);
                return result.list();
            } );
            return resultSet;
        }*/
        String entityLabel = "n1";
        String whereClause = this.graphQueryGenerator.generateWhereClause(entityLabel,criteria);
        String query = "MATCH ("+entityLabel+":"+entity+")\n" +
                whereClause +
                "RETURN "+entityLabel;
        System.out.println(query);
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

        //EntityCallback callback = this.callbackMap.get(entity);
        //callback.call(this,entity,json);
    }

    public void saveObjectRelationship(String entity,JsonObject json)
    {
        System.out.println("****SAVE_OBJECT_RELATIONSHIP****");
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
        System.out.println("****ESTABLISH_RELATIONSHIP****");
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
