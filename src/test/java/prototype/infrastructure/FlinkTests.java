package prototype.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FlinkTests {
    private static Logger logger = LoggerFactory.getLogger(FlinkTests.class);

    @Test
    public void dataStreamPrototype() throws Exception{
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Person> flintstones = env.fromElements(
                new Person("Fred", 35),
                new Person("Wilma", 35),
                new Person("Pebbles", 2));

        DataStream<Person> adults = flintstones.filter(
                new PersonFilter()
        );

        adults.print();

        env.execute();

        System.out.println("***********");
        System.out.println("DONE....");
        System.out.println("***********");
    }

    @Test
    public void dataStreamJsonPrototype() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<DataEvent> dataEvents = env.fromElements(
                new DataEvent(jsonString)
        );

        DataStream<DataEvent> ingestion = dataEvents.filter(
                new DataEventFilter()
        );

        ingestion.print();

        env.execute();
    }

    @Test
    public void dataStreamJsonArrayPrototype() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );

        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();
        List<DataEvent> inputEvents = new ArrayList<>();
        for(int i=0; i<jsonArray.size();i++) {
            inputEvents.add(new DataEvent(jsonArray.get(i).getAsJsonObject().toString()));
        }

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

        DataStream<DataEvent> ingestion = dataEvents.filter(
                new DataEventFilter()
        );

        ingestion.print();

        env.execute();
    }

    @Test
    public void dataStreamEndToEndPrototype() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input.json"),
                StandardCharsets.UTF_8
        );

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        List<DataEvent> inputEvents = new ArrayList<>();
        inputEvents.add(new DataEvent(jsonString));

        DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

        DataStream<DataEvent> mappedStream = dataEvents.map(
                new Phase1MapFunction()
        );

        mappedStream.map(new Phase2MapFunction())
                .addSink(new Phase3SinkFunction());

        env.execute();
    }

    @Test
    public void dataStreamEndToEndArrayPrototype() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        List<DataEvent> inputEvents = new ArrayList<>();
        for(int i=0; i<jsonArray.size();i++) {
            inputEvents.add(new DataEvent(jsonArray.get(i).getAsJsonObject().toString()));
        }

        DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

        DataStream<DataEvent> mappedStream = dataEvents.map(
                new Phase1MapFunction()
        );

        mappedStream.map(new Phase2MapFunction())
                .addSink(new Phase3SinkFunction());

        env.execute();
    }

    @Test
    public void dataStreamEndToEndArrayPrototypeWithContext() throws Exception{
        String jsonString = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream("ingestion/algorithm/input_array.json"),
                StandardCharsets.UTF_8
        );
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        List<DataEvent> inputEvents = new ArrayList<>();
        for(int i=0; i<jsonArray.size();i++) {
            inputEvents.add(new DataEvent(jsonArray.get(i).getAsJsonObject().toString()));
        }

        DataStream<DataEvent> dataEvents = env.fromCollection(inputEvents);

        //bootstrapped stream
        


        /*DataStream<DataEvent> mappedStream = dataEvents.map(
                new Phase1MapFunction()
        );

        mappedStream.map(new Phase2MapFunction())
                .addSink(new Phase3SinkFunction());*/

        env.execute();
    }
}
