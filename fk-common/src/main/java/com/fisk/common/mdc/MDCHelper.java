package com.fisk.common.mdc;


import com.fisk.common.constants.TraceConstant;
import org.slf4j.MDC;

/**
 * @author gy
 */
public class MDCHelper {

    public static void setAppLogType(TraceTypeEnum type) {
        MDC.put(TraceConstant.APP_LOGTYPE, type.getName());
    }

    public static void setFunction(String name) {
        MDC.put(TraceConstant.APP_FUNCTION, name);
    }

    public static void setClass(String name) {
        MDC.put(TraceConstant.APP_CLASS, name);
    }


}
