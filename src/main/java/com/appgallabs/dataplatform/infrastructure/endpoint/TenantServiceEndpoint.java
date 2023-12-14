package com.appgallabs.dataplatform.infrastructure.endpoint;

import com.appgallabs.dataplatform.common.ValidationException;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.infrastructure.TenantService;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tenant_manager")
public class TenantServiceEndpoint {
    private static Logger logger = LoggerFactory.getLogger(TenantServiceEndpoint.class);

    @Inject
    private TenantService tenantService;

    @Path("get_tenant/{apiKey}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTenant(@PathParam("apiKey") String apiKey){
        try {
            Tenant tenant = this.tenantService.getTenant(apiKey);

            JsonObject responseJson = tenant.toJson();

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("create_tenant")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTenant(@RequestBody String input){
        try {
            Tenant tenant = Tenant.parse(input);

            tenant = this.tenantService.createTenant(tenant.getName(), tenant.getEmail());

            JsonObject responseJson = tenant.toJson();

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }
        catch(ValidationException validationException){
            logger.error(validationException.getMessage(), validationException);
            JsonObject error = validationException.toJson();
            return Response.status(403).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}
