package prototype.infrastructure;


import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.state.api.functions.BroadcastStateBootstrapFunction;

public class InjestionBootstrapFunction extends BroadcastStateBootstrapFunction<DataEvent> {
    public static final MapStateDescriptor<String, String> objectHash =
            new MapStateDescriptor<>("objectHash", Types.STRING, Types.STRING);

    @Override
    public void processElement(DataEvent dataEvent, Context context) throws Exception {
        String object_hash = dataEvent.toString();
        context.getBroadcastState(objectHash).put("objectHash",object_hash);
    }
}
