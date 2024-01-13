package com.appgallabs.dataplatform.targetSystem.framework;

import com.appgallabs.dataplatform.targetSystem.core.driver.MySqlStoreDriver;
import com.appgallabs.dataplatform.util.Debug;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class StoreSinkFunction implements SinkFunction<String> {
    private StoreDriver storeDriver;

    public StoreSinkFunction(StoreDriver storeDriver) {
        this.storeDriver = storeDriver;
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        Debug.out("****STORE_SINK_FUNCTION*****");
        Debug.out(value);
        Debug.out("***************************");
        JsonArray data = new JsonArray();
        JsonObject dataObject = JsonUtil.validateJson(value).getAsJsonObject();
        data.add(dataObject);

        //TODO:
        //MySqlStoreDriver mySqlStoreDriver = new MySqlStoreDriver();
        //mySqlStoreDriver.storeData(data.toString());

        storeDriver.storeData(data);
    }
}
