package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

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

        //JSONObject obj = MessageHelper.getMessageLogEntryJSON(source, topicName,message.key(),message.value());
        //log.info("*****CONSUMER*******");
        //log.info(position);
        //log.info(message.key());
        //log.info(message.value());
        //log.info("********************");

        /**
         * TODO: Integrate with the Pipeline Service
         */
        String entity = "books";
        String messageValue = message.value();
        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();

        String payload = json.get("message").getAsString();
        JsonElement payloadElem = JsonParser.parseString(payload);

        String jsonPayloadString = payloadElem.toString();

        //SecurityToken
        String securityTokenString = json.get("securityToken").getAsString();
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenString);

        this.pipelineService.ingest(securityToken,entity,jsonPayloadString);
    }
}
