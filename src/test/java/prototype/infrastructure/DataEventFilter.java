package prototype.infrastructure;

import org.apache.flink.api.common.functions.FilterFunction;

import java.io.Serializable;

public class DataEventFilter implements FilterFunction<DataEvent>, Serializable {

    @Override
    public boolean filter(DataEvent dataEvent) throws Exception {
        return true;
    }
}
