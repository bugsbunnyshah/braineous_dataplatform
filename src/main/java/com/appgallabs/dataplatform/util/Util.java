package com.appgallabs.dataplatform.util;

import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

public class Util {

    public static String loadResource(String resource) throws Exception{
        String json = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().getResourceAsStream(resource),
                StandardCharsets.UTF_8
        );
        return json;
    }
}
