package prototype.infrastructure;

import org.apache.flink.api.common.functions.FilterFunction;

import java.io.Serializable;

public class PersonFilter implements FilterFunction<Person>, Serializable {
    @Override
    public boolean filter(Person person) throws Exception {
        return person.age <= 18;
    }
}
