package com.appgallabs.dataplatform.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.charset.StandardCharsets;

public class ObjectUtil {

    public static int hashCode(String object)
    {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(object.getBytes(StandardCharsets.UTF_8));
        return hashCodeBuilder.build().intValue();
    }
}
