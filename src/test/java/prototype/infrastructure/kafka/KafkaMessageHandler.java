package prototype.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * The interface KafkaMessageHandler.
 *
 * This interface is the template callback functions that can
 * be passed to an instance of the {@link com.demo.kafka.SimpleConsumer}
 */
@FunctionalInterface
public interface KafkaMessageHandler {
    /**
     * The method that defines the message processing behavior
     *
     * @param topicName The name of the topic being consumed
     * @param message   The message that was consumed
     * @throws Exception Thrown if an exception occurs
     */
    void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception;
}
