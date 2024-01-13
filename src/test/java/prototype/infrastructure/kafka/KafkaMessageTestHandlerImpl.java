package prototype.infrastructure.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * The class KafkaMessageTestHandlerImpl is the callback functions that's
 * using when running producer and consumer tests that require a callback
 * function.
 *
 * The class runs assertions against the message passed to the method named
 * processMessage()
 */
public class KafkaMessageTestHandlerImpl implements KafkaMessageHandler{

    static Logger log = LoggerFactory.getLogger(KafkaMessageTestHandlerImpl.class);
    private int numberOfCalls = 0;

    @Override
    public void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception {
        Assert.assertNotNull(message);
        String position = message.partition() + "-" + message.offset();

        Assert.assertEquals(message.key().getClass(),String.class);
        Assert.assertEquals(message.value().getClass(),String.class);
        Assert.assertEquals(topicName, message.topic());

        String source = KafkaMessageHandlerImpl.class.getName();
        JSONObject obj = MessageHelper.getMessageLogEntryJSON(source, topicName,message.key(),message.value());
        setNumberOfCalls(getNumberOfCalls() + 1);

        log.info(obj.toJSONString());

        obj = MessageHelper.getSimpleJSONObject("The number of calls is: " + String.valueOf(getNumberOfCalls()));

        log.info(obj.toJSONString());
    }

    /**
     * Gets number of calls for the producer or consumer to make
     *
     * @return the number of calls
     */
    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    /**
     * Sets number of calls for the producer or consumer to make
     *
     * @param numberOfCalls the number of calls
     */
    public void setNumberOfCalls(int numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }
}