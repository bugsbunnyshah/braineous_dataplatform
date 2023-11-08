package com.appgallabs.dataplatform.client.sdk.infrastructure;

import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.client.sdk.infrastructure.StreamingAgent;
import org.junit.jupiter.api.Test;

public class StreamingAgentTests {

    //TODO: solidify (CR2)
    @Test
    public void sendData() throws Exception{
        StreamingAgent streamingAgent = StreamingAgent.getInstance();

        for(int i=0; i<10; i++) {
            String pipeId = "123";
            String entity = TempConstants.ENTITY;
            streamingAgent.sendData(pipeId, entity, "{hello"+i);
        }
    }
}
