package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

/**
 *
 * The class implements the processMessage() method. Typically, this class is used
 * to supply callback behavior for this project's producers and consumers.
 */
public class KafkaMessageHandlerImpl implements KafkaMessageHandler {
    static Logger log = Logger.getLogger(KafkaMessageHandlerImpl.class.getName());

    @Override
    public void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception {
        String position = "PARTITION: " + message.partition() + "-" + "OFFSET: " + message.offset();
        String source = KafkaMessageHandlerImpl.class.getName();
        JSONObject obj = MessageHelper.getMessageLogEntryJSON(source, topicName,message.key(),message.value());

        //log.info("*****CONSUMER*******");
        //log.info(position);
        //log.info(obj.toJSONString());
        //log.info("********************");

        /**
         * TODO: Integrate with the Pipeline Service
         */
        String messageValue = message.value();
        JsonElement json = JsonParser.parseString(messageValue);
        JsonUtil.printStdOut(json);
    }
}
