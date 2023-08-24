package prototype.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type SimpleConsumer is a class the demonstrates how to consume messages
 * from a Kafka cluster. The class provides functionality for the
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer}.
 */
class SimpleConsumer extends AbstractSimpleKafka{

    private final int TIME_OUT_MS = 5000;
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private final AtomicBoolean closed = new AtomicBoolean(false);


    /**
     * The class's Log4J logger
     */
    static Logger log = Logger.getLogger(SimpleConsumer.class.getName());

    /**
     * Instantiates a new instance based on Abstract class SimpleKafka.
     * <p>
     * This abstract class's constructor provides graceful
     * shutdown behavior for Kafka producers and consumers
     */
    public SimpleConsumer() throws Exception {
    }

    /**
     * The run method retrieves a collection of ConsumerRecords. The number of
     * ConsumerRecords retrieved is defined according to max.poll.records which
     * is defined in the file, config.properties.
     *
     * This method is provided as a convenience for testing purposes. It does
     * not use the KafkaConsumer internal to the class.
     *
     * @param topicName    the topic to access
     * @param callback the callback function that processes messages retrieved
     *                 from Kafka
     * @param numberOfRecords Optional, the max number of records to retrieve during the
     *                        run to the consumer. If nul, the  number of records will be that
     *                        value defined in max.poll.records as defined in config.properties
     * @throws Exception the Exception that will get thrown upon an error
     */
    void run(String topicName, KafkaMessageHandler callback, Integer numberOfRecords) throws Exception {
        Properties props = PropertiesHelper.getProperties();
        //See if the number of records is provided
        Optional<Integer> recs = Optional.ofNullable(numberOfRecords);

        //adjust the number of records to get accordingly
        Integer numOfRecs = recs.orElseGet(() -> Integer.parseInt(props.getProperty("max.poll.records")));
        props.setProperty("max.poll.records", String.valueOf(numOfRecs));

        // create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        //make the consumer available for graceful shutdown
        setKafkaConsumer(consumer);
        consumer.assign(Collections.singleton(new TopicPartition(topicName, 0)));

        int recNum = numOfRecs;
        while (recNum > 0) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(TIME_OUT_MS));
            recNum = records.count();
            if (recNum == 0) {
                log.info(MessageHelper.getSimpleJSONObject("No records retrieved"));
                break;
            }

            for (ConsumerRecord<String, String> record : records) {
                callback.processMessage(topicName, record);
                recNum--;
            }
        }

        consumer.close();
    }

    private void close() throws Exception {
        if (this.getKafkaConsumer() == null){
            log.info(MessageHelper.getSimpleJSONObject("The internal consumer is NULL"));
            return;
        }
        log.info(MessageHelper.getSimpleJSONObject("Closing consumer"));
        if( this.getKafkaConsumer() != null) this.getKafkaConsumer().close();
    }

    /**
     * The runAlways method retrieves a collection of ConsumerRecords continuously.
     * The number of max number of records retrieved in each polling session back to
     * the Kafka broker is defined by the property max.poll.records as published by
     * the class {@link com.demo.kafka.PropertiesHelper} object
     *
     * @param topicName    the topic to access
     * @param callback the callback function that processes messages retrieved
     *                 from Kafka
     * @throws Exception the Exception that will get thrown upon an error
     */
    public void runAlways(String topicName, KafkaMessageHandler callback) throws Exception {
        Properties props = PropertiesHelper.getProperties();
        //make the consumer available for graceful shutdown
        setKafkaConsumer(new KafkaConsumer<>(props));

        //keep running forever or until shutdown() is called from another thread.
        try {
            getKafkaConsumer().subscribe(List.of(topicName));
            while (!closed.get()) {
                ConsumerRecords<String, String> records =
                        getKafkaConsumer().poll(Duration.ofMillis(TIME_OUT_MS));
                if (records.count() == 0) {
                    log.info(MessageHelper.getSimpleJSONObject("No records retrieved"));
                }

                for (ConsumerRecord<String, String> record : records) {
                    callback.processMessage(topicName, record);
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        }
    }
    /**
     * Shuts down the internal {@link org.apache.kafka.clients.consumer.KafkaConsumer}
     * This method is provided as a convenience for shutting down the consumer when
     * invoked using SimpleConsumer.runAlways().
     * @throws Exception the Exception that will get thrown upon an error
     */
    public void shutdown() throws Exception {
        closed.set(true);
        log.info(MessageHelper.getSimpleJSONObject("Shutting down consumer"));
        getKafkaConsumer().wakeup();
    }

    public KafkaConsumer<String, String> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public void setKafkaConsumer(KafkaConsumer<String, String> kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }
}
