package com.fisk.auth.annotation;

import com.fisk.auth.config.MvcConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Lock
 * @date 2021/5/17 15:46
 *
 * 让Spring来扫描MvcConfiguration拦截器,启用JWT验证开关,
 * 会通过mvc的拦截器拦截用户请求,并获取用户信息,存入UserContext
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MvcConfiguration.class)
@Documented
@Inherited
public @interface EnableJwtVerification {
}
