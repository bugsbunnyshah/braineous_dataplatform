package prototype.graphql;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

//@QuarkusTest
public class SandboxTests {
    private static Logger logger = LoggerFactory.getLogger(SandboxTests.class);

    @Test
    public void helloWorld(){
        String schema = "type Query{hello: String}";

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("world")))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{hello}");

        System.out.println(executionResult.getData().toString());
    }

    @Test
    public void helloBatching() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("graphql/batching.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();
        //JsonUtil.printStdOut(jsonArray);

        GraphQLDataFetchers fetchers = new GraphQLDataFetchers();
        GraphQLProvider provider = new GraphQLProvider(fetchers);
        provider.init();
        GraphQL graphQL = provider.graphQL();

        //System.out.println(provider.globalDataLoaderRegistry(1000l,1000l));

        /*GraphQLInvocationData invocationData = new GraphQLInvocationData("{}","queryAll", null);
        RequestScopedGraphQLInvocation invocation = new RequestScopedGraphQLInvocation(graphQL,fetchers);

        CompletableFuture<ExecutionResult> result = invocation.invoke(invocationData, null);
        System.out.println(result.get().isDataPresent());*/
    }
}
