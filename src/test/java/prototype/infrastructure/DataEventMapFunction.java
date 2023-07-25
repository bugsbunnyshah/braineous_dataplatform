package prototype.infrastructure;

import org.apache.flink.api.common.functions.MapFunction;

public class DataEventMapFunction implements MapFunction<DataEvent,SinkEvent> {
    @Override
    public SinkEvent map(DataEvent dataEvent) throws Exception {
        SinkEvent sinkEvent = new SinkEvent(dataEvent.getJson());
        return sinkEvent;
    }
}
