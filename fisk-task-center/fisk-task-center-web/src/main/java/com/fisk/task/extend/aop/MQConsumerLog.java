package com.fisk.task.extend.aop;

import com.fisk.common.framework.mdc.TraceTypeEnum;

import java.lang.annotation.*;

/**
 * @author gy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MQConsumerLog {
    TraceTypeEnum type() default TraceTypeEnum.UNKNOWN;

    boolean sendMsg() default true;

    /**
     * 弹框:默认1环绕,2前置,3后置
     * @return
     */
    int notificationType() default 1;
}