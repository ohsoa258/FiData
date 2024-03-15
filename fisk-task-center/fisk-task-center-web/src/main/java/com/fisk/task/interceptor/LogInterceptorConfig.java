package com.fisk.task.interceptor;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class LogInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private LogInterceptor logInterceptor;

    /**
     * 配置拦截器要拦截的请求
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除某些请求，例如静态资源、swagger等
                .excludePathPatterns(
                        "/static/**"
                        , "/css/**"
                        , "/js/**"
                        , "/swagger-ui/**"
                        , "/swagger-ui.html"
                        , "/swagger-resources/**"
                        , "/error"
                        , "/csrf"
                        , "/"
                        , "/webjars/**");
    }
}