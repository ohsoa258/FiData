package com.fisk.task.extend.aop;

import com.fisk.common.mdc.TraceTypeEnum;

import java.lang.annotation.*;

/**
 * @author gy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MQConsumerLog {
    TraceTypeEnum type() default TraceTypeEnum.UNKNOWN;
}