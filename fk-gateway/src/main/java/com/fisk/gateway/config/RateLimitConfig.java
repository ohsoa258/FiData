package com.fisk.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author: Lock
 * @data: 2021/5/14 15:45
 */
@Configuration  // 声明此类为配置类(==配置文件)
public class RateLimitConfig {


    @Bean // 将KeyResolver注入到IOC容器中,以方法名作为Bean名称使用
    public KeyResolver ipKeyResolver() {

        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {

                // 给不同的请求IP地址设置不同令牌桶
                return Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
            }
        };
    }

}
