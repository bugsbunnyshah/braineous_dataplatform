package com.appgallabs.dataplatform.preprocess;

import com.appgallabs.dataplatform.history.service.DataReplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Priority(2)
@Provider
public class AITrafficAgent implements ContainerRequestFilter, ContainerResponseFilter
{
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgent.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private AITrafficContainer aiTrafficContainer;

    private List<String> diffChains = new ArrayList<>();

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        //logger.info("*****PATH**********");
        //logger.info(context.getUriInfo().getRequestUri().getPath());
        //logger.info("*****PATH**********");
        /*if(!context.getUriInfo().getRequestUri().getPath().startsWith("/liveModel") &&
                !context.getUriInfo().getRequestUri().getPath().startsWith("/trainModel") &&
                !context.getUriInfo().getRequestUri().getPath().startsWith("/remoteModel")
        )
        {
            return;
        }*/

        /*String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        if(payload == null || payload.length() == 0)
        {
            return;
        }

        JsonElement input;
        try
        {
           input  = JsonParser.parseString(payload);
           if(!input.isJsonObject() && !input.isJsonArray())
           {
               return;
           }
        }
        catch (Exception e)
        {
            return;
        }*/

        //logger.info("TRAFFIC***********************");
        //logger.info(payload);
        //logger.info("TRAFFIC***********************");

        /*String modelId = JsonParser.parseString(payload).getAsJsonObject().get("modelId").getAsString();
        String key = "/"+this.securityTokenContainer.getSecurityToken().getPrincipal()+"/"+modelId;
        if(!this.diffChains.contains(key))
        {
            String diffChainId;
            if(input.isJsonObject())
            {
                diffChainId = this.dataReplayService.generateDiffChain(input.getAsJsonObject());
            }
            else
            {
                diffChainId = this.dataReplayService.generateDiffChain(input.getAsJsonArray());
            }
            this.diffChains.add(diffChainId);
        }
        else
        {
            if(input.isJsonObject())
            {
                this.dataReplayService.addToDiffChain(key, input.getAsJsonObject());
            }
            else
            {
                this.dataReplayService.addToDiffChain(key, input.getAsJsonArray());
            }
        }

        this.aiTrafficContainer.setChainId(key);
        context.setEntityStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));*/
    }

    @Override
    public void filter(ContainerRequestContext context, ContainerResponseContext containerResponseContext) throws IOException
    {
        /*if(!context.getUriInfo().getRequestUri().getPath().startsWith("/liveModel") &&
                !context.getUriInfo().getRequestUri().getPath().startsWith("/trainModel") &&
                !context.getUriInfo().getRequestUri().getPath().startsWith("/remoteModel")
        )
        {
            return;
        }

        //Process the response
        Object entity = containerResponseContext.getEntity();
        if(entity == null)
        {
            return;
        }
        String entityString = entity.toString();
        if(entityString == null || entityString.length() == 0)
        {
            return;
        }

        JsonElement output;
        try
        {
            output  = JsonParser.parseString(entityString);
            if(!output.isJsonObject() && !output.isJsonArray())
            {
                return;
            }
        }
        catch (Exception e)
        {
            return;
        }

        String responseChainId = this.getResponseChainId();
        if(responseChainId == null)
        {
            if(output.isJsonObject()) {
                responseChainId = this.dataReplayService.generateDiffChain(output.getAsJsonObject());
            }
            else
            {
                responseChainId = this.dataReplayService.generateDiffChain(output.getAsJsonArray());
            }
            this.setResponseChainId(responseChainId);
        }
        else
        {
            String requestChainId = this.getRequestChainId();
            if(output.isJsonObject()) {
                JsonObject outputJson = output.getAsJsonObject();
                this.dataReplayService.addToDiffChain(requestChainId, responseChainId, outputJson);
            }
            else
            {
                JsonArray outputArray = output.getAsJsonArray();
                this.dataReplayService.addToDiffChain(requestChainId, responseChainId, outputArray);
            }
        }*/
    }
}
