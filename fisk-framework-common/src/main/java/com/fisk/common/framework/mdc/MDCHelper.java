package com.fisk.common.framework.mdc;


import com.fisk.common.core.constants.TraceConstant;
import org.slf4j.MDC;

import java.util.UUID;

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

    public static String setTraceId() {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TraceConstant.TRACE_ID, traceId);
        return traceId;
    }

    public static void clear() {
        MDC.clear();
    }
}
