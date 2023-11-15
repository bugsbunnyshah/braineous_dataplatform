package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.receiver.framework.StoreOrchestrator;
import com.appgallabs.dataplatform.util.Debug;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;

/**
 *
 * The class implements the processMessage() method. Typically, this class is used
 * to supply callback behavior for this project's producers and consumers.
 */
public class EventHandler implements KafkaMessageHandler {
    static Logger log = Logger.getLogger(EventHandler.class.getName());

    private PipelineService pipelineService;
    private StoreOrchestrator storeOrchestrator;

    public EventHandler(PipelineService pipelineService, StoreOrchestrator storeOrchestrator){
        this.pipelineService = pipelineService;
        this.storeOrchestrator = storeOrchestrator;
    }

    @Override
    public void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception {
        String position = "PARTITION: " + message.partition() + "-" + "OFFSET: " + message.offset();
        String messageValue = message.value();
        String pipeId = topicName;

        Debug.out("************************");
        Debug.out("position: "+position);
        Debug.out("message: "+messageValue);
        Debug.out("************************");

        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();

        String payload = json.get("message").getAsString();
        JsonElement payloadElem = JsonParser.parseString(payload);

        String jsonPayloadString = payloadElem.toString();

        //SecurityToken
        String securityTokenString = json.get("securityToken").getAsString();
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenString);

        //Entity
        String entity = json.get("entity").getAsString();

        JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();
        this.pipelineService.ingest(securityToken, datalakeDriverConfiguration.toString(),
                pipeId,entity,jsonPayloadString);

        this.storeOrchestrator.receiveData(securityToken, pipeId,jsonPayloadString);
    }
}
