package prototype.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.spring.web.servlet.GraphQLInvocation;
import graphql.spring.web.servlet.GraphQLInvocationData;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.web.context.request.WebRequest;

import java.util.concurrent.CompletableFuture;

public class RequestScopedGraphQLInvocation implements GraphQLInvocation {

    private final GraphQL graphQL;
    private final GraphQLDataFetchers graphQLDataFetchers;

    public RequestScopedGraphQLInvocation(GraphQL graphQL, GraphQLDataFetchers graphQLDataFetchers) {
        this.graphQL = graphQL;
        this.graphQLDataFetchers = graphQLDataFetchers;
    }

    @Override
    public CompletableFuture<ExecutionResult> invoke(GraphQLInvocationData invocationData, WebRequest webRequest) {
        ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
          .query(invocationData.getQuery())
          .operationName(invocationData.getOperationName())
          .variables(invocationData.getVariables());

        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        DataLoader<String, CountryTO> countryLoader = DataLoader.newDataLoader(graphQLDataFetchers.countryBatchLoader());
        dataLoaderRegistry.register("countries", countryLoader);

        executionInputBuilder.dataLoaderRegistry(dataLoaderRegistry);
        executionInputBuilder.context(dataLoaderRegistry);

        ExecutionInput executionInput = executionInputBuilder.build();
        return graphQL.executeAsync(executionInput);
    }
}