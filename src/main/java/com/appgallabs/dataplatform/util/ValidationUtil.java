package com.appgallabs.dataplatform.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {

    public static boolean isEmailValid(String email){
        Pattern p = Pattern.compile(".+@.+\\..+");//. represents single character
        Matcher m = p.matcher(email);
        boolean b = m.matches();
        return b;
    }
}
