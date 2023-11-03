package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The type SimpleProducer is a wrapper class for {@link KafkaProducer}.
 * The object publishes methods that send messages that have random string
 * content onto the Kafka broker defined in {@link /src/resources/config.properties}
 */
class SimpleProducer extends AbstractSimpleKafka {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Logger log = Logger.getLogger(SimpleProducer.class.getName());

    private KafkaProducer<String, String> kafkaProducer;
    private String topicName = null;

    /**
     * Instantiates a new Abstract class, SimpleKafka.
     * <p>
     * This abstract class's constructor provides graceful
     * shutdown behavior for Kafka producers and consumers
     *
     * @throws Exception the exception
     */
    private SimpleProducer() throws Exception {
    }

    public static SimpleProducer getInstance(){
        try {
            return new SimpleProducer();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    private void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    private String getTopicName() {
        return this.topicName;
    }

    public void shutdown() throws Exception {
        closed.set(true);
        log.info(MessageHelper.getSimpleJSONObject("Shutting down producer"));
        getKafkaProducer().close();
    }

    public void publishToBroker(SecurityTokenContainer securityTokenContainer,
                                String topicName, String message) throws Exception {
        String key = UUID.randomUUID().toString();
        this.send(securityTokenContainer,topicName, key, message);
    }

    /**
     * Does the work of sending a message to
     * a Kafka broker. The method uses the name of
     * the topic that was declared in this class's
     * constructor.
     *
     * @param topicName the name of the topic to where the message                   will be sent
     * @param key       the key value for the message
     * @param message   the content of the message
     * @throws Exception the exception that gets thrown upon error
     */
    protected void send(SecurityTokenContainer securityTokenContainer, String topicName, String key, String message) throws Exception {
        String source = SimpleProducer.class.getName();

        //Use the helper to create an informative log entry in JSON format
        JSONObject obj = MessageHelper.getMessageLogEntryJSON(securityTokenContainer.getSecurityToken(), source, topicName, key, message);
        //log.info(obj.toJSONString());

        //create the ProducerRecord object which will
        //represent the message to the Kafka broker.
        String messageString = obj.toJSONString();
        ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>(topicName, key, messageString);

        //Send the message to the Kafka broker using the internal
        //KafkaProducer
        getKafkaProducer().send(producerRecord);
    }

    /**
     * This method sends a limited number of messages
     * with random string data to the Kafka broker.
     *
     * This method is provided for testing purposes.
     *
     * @param topicName the name of the topic to where messages
     *                  will be sent
     * @param numberOfMessages the number of messages to send
     * @throws Exception the exception that gets raised upon error
     */
    public void run(String topicName, int numberOfMessages) throws Exception {
        int i = 0;
        while (i <= numberOfMessages) {
            String key = UUID.randomUUID().toString();
            String message = MessageHelper.getRandomString();
            //this.send(topicName, key, message);
            i++;
            Thread.sleep(100);
        }
        this.shutdown();
    }

    /**
     * The runAlways method sends a message to a topic.
     *
     * @param topicName    the name of topic to access
     * @param callback the callback function that processes messages retrieved
     *                 from Kafka
     * @throws Exception the Exception that will get thrown upon an error
     */
    public void runAlways(String topicName, KafkaMessageHandler callback) throws Exception {
        while (true) {
            String key = UUID.randomUUID().toString();
            //use the Message Helper to get a random string
            String message = MessageHelper.getRandomString();
            //send the message
            //this.send(topicName, key, message);
        }
    }

    private KafkaProducer<String, String> getKafkaProducer() throws Exception {
        if (this.kafkaProducer == null) {
            Properties props = PropertiesHelper.getProperties();
            this.kafkaProducer = new KafkaProducer<>(props);
        }
        return this.kafkaProducer;
    }
}
