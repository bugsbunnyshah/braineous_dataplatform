package prototype.infrastructure;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class DataEventSinkFunction implements SinkFunction<SinkEvent> {

    @Override
    public void invoke(SinkEvent value, Context context) throws Exception {
        SinkFunction.super.invoke(value, context);
        System.out.println("*******SINK_INVOKE******");
        System.out.println(value);
    }
}
