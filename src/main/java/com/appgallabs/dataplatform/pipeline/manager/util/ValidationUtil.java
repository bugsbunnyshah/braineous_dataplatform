package com.appgallabs.dataplatform.pipeline.manager.util;

import com.appgallabs.dataplatform.pipeline.manager.InvalidPipeIdException;
import org.apache.commons.lang3.StringUtils;

public class ValidationUtil {

    public static void validatePipeId(String pipeId) throws InvalidPipeIdException{
        boolean isAlpha = StringUtils.isAlpha(pipeId);

        if(!isAlpha) {
            throw new InvalidPipeIdException();
        }
    }
}
