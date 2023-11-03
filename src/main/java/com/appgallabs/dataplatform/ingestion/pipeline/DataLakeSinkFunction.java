package com.appgallabs.dataplatform.ingestion.pipeline;

import com.appgallabs.dataplatform.TempConstants;
import com.appgallabs.dataplatform.datalake.MongoDBDataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.util.JsonUtil;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonObject;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class DataLakeSinkFunction implements SinkFunction<String> {

    private SecurityToken securityToken;

    public DataLakeSinkFunction(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        MongoDBDataLakeDriver driver = new MongoDBDataLakeDriver();
        driver.storeIngestion(null, null);

        /*JsonObject object = JsonUtil.validateJson(value).getAsJsonObject();

        //for timestamp
        OffsetDateTime ingestionTime = OffsetDateTime.now(ZoneOffset.UTC);
        Long timestamp = ingestionTime.toEpochSecond();

        //objectHash
        String objectHash = JsonUtil.getJsonHash(object);

        //tenant
        Tenant tenant = new Tenant(this.securityToken.getPrincipal());
        String tenantString = tenant.toString();

        //TODO: (CR1)
        String entity = TempConstants.ENTITY;

        //store into datalake
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(value);

        //FileUtils.write(new File("flattenJson.debug"), flattenJson.toString(),
        //        StandardCharsets.UTF_8);

        int count = 0;
        Set<Map.Entry<String, Object>> entries = flattenJson.entrySet();
        for(Map.Entry<String, Object> entry: entries){
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue().toString();

            Map<String,Object> fieldMap = new HashMap<>();
            fieldMap.put("tenant",tenantString);
            fieldMap.put("objectHash",objectHash);
            fieldMap.put("timestamp",timestamp);
            fieldMap.put("entity",entity);
            fieldMap.put(fieldName,fieldValue);

            Registry registry = Registry.getInstance();
            System.out.println("**********************");
            System.out.println(registry);
            System.out.println("**********************");

            //TODO
            //String datalakeId = this.dataLakeDriver.storeIngestion(tenant, fieldMap);

            //FileUtils.write(new File("datalakeId"+count+".debug"), datalakeId,
            //        StandardCharsets.UTF_8);
            //count++;
        }*/

        /*if(true){
            throw new NullPointerException("why not (back_bhenchod2.0)?");
        }*/
    }
}
