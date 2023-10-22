package com.appgallabs.dataplatform.client.sdk.api;

import org.junit.jupiter.api.Test;

public class StreamingAgentTests {

    @Test
    public void sendData() throws Exception{
        StreamingAgent streamingAgent = StreamingAgent.getInstance();

        for(int i=0; i<10; i++) {
            streamingAgent.sendData("hello"+i);
        }
    }
}
