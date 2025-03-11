package prototype.cdc;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;
import test.components.Util;

import java.util.ArrayList;
import java.util.Collection;

public class CDCProcessPrototype {

    @Test
    public void process() throws Exception{
        //get the change dataset
        String objStr = Util.loadResource("cdc/obj1_array.json");
        JsonArray objJsonArray = JsonUtil.validateJson(objStr).getAsJsonArray();
        JsonUtil.printStdOut(objJsonArray);

        Collection<String> objCollection = new ArrayList<>();
        for(int i=0; i<objJsonArray.size(); i++){
            JsonObject objJson = objJsonArray.get(i).getAsJsonObject();
            String objJsonStr = objJson.toString();
            objCollection.add(objJsonStr);
        }

        System.out.println(objCollection);

        //process each record in parallel to achieve O(1) time complexity using Flink
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> sourceData = env.fromCollection(
                objCollection
        );
        sourceData.print();

        DataStream<String> parallel = sourceData.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                //TODO: execute the CDC process/algorithm for each record
                boolean isInsert = isInsert();
                if(isInsert){
                    //insert the record into the live target store
                }else{
                    //update the record in the live target store
                }

                return s;
            }
        });
        parallel.print();

        //execute the job graph
        env.execute();
    }

    private boolean isInsert(){
        return false;
    }

    private void insertRecord(){

    }

    private void updateRecord(){

    }
}
