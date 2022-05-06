package com.fisk.common.framework.advice;

import java.lang.annotation.*;

/**
 * @author gy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerAOPConfig {
    boolean printParams() default true;
}
