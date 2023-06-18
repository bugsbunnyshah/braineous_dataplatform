package com.appgallabs.dataplatform.ingestion.algorithm;


import com.github.wnameless.json.flattener.JsonFlattener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class SchemalessMapper {
    private static Logger logger = LoggerFactory.getLogger(SchemalessMapper.class);

    public Map<String,Object> mapAll(String json){
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
        return flattenJson;
    }
}
