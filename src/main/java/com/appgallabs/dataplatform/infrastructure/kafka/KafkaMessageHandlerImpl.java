package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

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
public class KafkaMessageHandlerImpl implements KafkaMessageHandler {
    static Logger log = Logger.getLogger(KafkaMessageHandlerImpl.class.getName());

    private PipelineService pipelineService;

    public KafkaMessageHandlerImpl(PipelineService pipelineService){
        this.pipelineService = pipelineService;
    }

    @Override
    public void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception {
        String position = "PARTITION: " + message.partition() + "-" + "OFFSET: " + message.offset();
        String source = KafkaMessageHandlerImpl.class.getName();

        //  TODO: unhardcode entity (CR1)
        String entity = TempConstants.ENTITY;
        String messageValue = message.value();
        System.out.println("****KAFKA_DEBUG**********");
        System.out.println(messageValue);
        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();

        String payload = json.get("message").getAsString();
        JsonElement payloadElem = JsonParser.parseString(payload);

        String jsonPayloadString = payloadElem.toString();

        //SecurityToken
        String securityTokenString = json.get("securityToken").getAsString();
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenString);

        System.out.println("****KAFKA_DEBUG**********");
        System.out.println(jsonPayloadString);

        JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();
        this.pipelineService.ingest(securityToken, datalakeDriverConfiguration.toString(),entity,jsonPayloadString);
    }
}
