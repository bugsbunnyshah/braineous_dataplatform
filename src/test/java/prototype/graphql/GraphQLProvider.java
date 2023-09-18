package prototype.graphql;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;

import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class GraphQLProvider {

    private final GraphQLDataFetchers graphQLDataFetchers;

    private GraphQL graphQL;

    public GraphQLProvider(GraphQLDataFetchers graphQLDataFetchers) {
        this.graphQLDataFetchers = graphQLDataFetchers;
    }

    public GraphQL graphQL() {
        return graphQL;
    }


    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    public DataLoaderRegistry globalDataLoaderRegistry(long maxCacheSize, long expiryInSeconds) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();

        //CacheMap customCache = new CustomGuavaBasedCache(maxCacheSize, expiryInSeconds);
        DataLoaderOptions options = DataLoaderOptions.newOptions();

        DataLoader<String, CountryTO> countryLoader = DataLoader.newDataLoader(graphQLDataFetchers.countryBatchLoader(), options);
        dataLoaderRegistry.register("countries", countryLoader);
        return dataLoaderRegistry;
    }

    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("animals", graphQLDataFetchers.animalsFetcher())
                )
                .type(newTypeWiring("Animal")
                        .dataFetcher("countries", graphQLDataFetchers.animalCountriesFetcher())
                )
                .build();
    }
}
