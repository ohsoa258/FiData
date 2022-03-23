package com.fisk.common.mdc;

import java.lang.annotation.*;

/**
 * @author gy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceType {
    TraceTypeEnum type() default TraceTypeEnum.UNKNOWN;
}
