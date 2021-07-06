package com.fisk.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Lock
 * @date 2021/5/14 15:45
 */
@Configuration
public class RateLimitConfig {


    @Bean
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
