package com.fisk.common.framework.mdc;


import com.fisk.common.core.constants.TraceConstant;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author gy
 */
public class MDCHelper {

    public static String get(String key) {
        return MDC.get(key);
    }

    public static void setAppLogType(TraceTypeEnum type) {
        MDC.put(TraceConstant.APP_LOGTYPE, type.getName());
    }

    @Deprecated
    public static void setFunction(String name) {
        MDC.put(TraceConstant.APP_FUNCTION, name);
    }

    @Deprecated
    public static void setClass(String name) {
        MDC.put(TraceConstant.APP_CLASS, name);
    }

    public static String setTraceId() {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TraceConstant.TRACE_ID, traceId);
        return traceId;
    }

    public static String getTraceId() {
        return MDC.get(TraceConstant.TRACE_ID);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    public static void removeLogType() {
        MDC.remove(TraceConstant.APP_LOGTYPE);
    }

    public static void removeTraceId() {
        MDC.remove(TraceConstant.TRACE_ID);
    }

    public static void clear() {
        MDC.clear();
    }
}
