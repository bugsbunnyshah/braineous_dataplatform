package prototype.infrastructure;

import org.apache.flink.api.common.functions.MapFunction;

public class Phase2MapFunction implements MapFunction<DataEvent,SinkEvent> {

    @Override
    public SinkEvent map(DataEvent dataEvent) throws Exception {
        System.out.println("*******PHASE2_INVOKE******");
        SinkEvent nextEvent = new SinkEvent(dataEvent.getJson());
        return nextEvent;
    }
}
