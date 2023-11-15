package prototype.infrastructure;

import org.apache.flink.api.common.functions.MapFunction;

public class Phase1MapFunction implements MapFunction<DataEvent,DataEvent> {
    @Override
    public DataEvent map(DataEvent dataEvent) throws Exception {
        System.out.println("*******PHASE1_INVOKE******");
        DataEvent nextEvent = new DataEvent(dataEvent.getJson());
        return nextEvent;
    }
}
