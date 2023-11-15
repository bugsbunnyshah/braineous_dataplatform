package prototype.infrastructure;

import org.apache.flink.api.common.functions.FilterFunction;

import java.io.Serializable;

public class PersonFilter implements FilterFunction<String>, Serializable {
    @Override
    public boolean filter(String age) throws Exception {
        System.out.println("FILTER ACTIVATED: "+ age);
        return true;
    }
}
