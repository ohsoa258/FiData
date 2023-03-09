package com.fisk.datamanagement.aop;

import java.lang.annotation.*;

/**
 * 日志注解 指定类型
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    // 用于存储操作日志行为描述
    String value() default "";
}
