package prototype.infrastructure.kafka;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Abstract class SimpleKafka
 */
public abstract class AbstractSimpleKafka {
    private final static Logger log = LoggerFactory.getLogger(AbstractSimpleKafka.class.getName());
    /**
     * Instantiates a new Abstract class, SimpleKafka.
     * <p>
     * This abstract class's constructor provides graceful
     * shutdown behavior for Kafka producers and consumers
     *
     * @throws Exception the exception
     */
    public AbstractSimpleKafka() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        log.info(MessageHelper.getSimpleJSONObject("Created the Shutdown Hook").toJSONString());
    }

    /**
     * The inherited classes will provide the behavior necessary
     * to shut down gracefully.
     *
     * @throws Exception the exception that get thrown upon error
     */
    public abstract void shutdown() throws Exception;

    /**
     * This purpose of this method is to provide continuous
     * behavior to produce or consume messages from a Kafka
     * broker
     *
     * @param topicName    the topicName to execute against
     * @param callback a callback function to provide processing
     *                 logic after a message is produced or after
     *                 a message is consumed
     * @throws Exception the exception that get thrown upon error
     */
    public abstract void runAlways(String topicName, KafkaMessageHandler callback) throws Exception;
}
