package com.fisk.gateway.filters;

import com.fisk.auth.constants.JwtConstants;
import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: Lock
 * @data: 2021/5/17 17:16
 * 拦截器: 用于拦截用户请求,并刷新有效期
 */
@Slf4j
@Component
public class LoginFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.获取Request对象
        ServerHttpRequest request = exchange.getRequest();
        // 2.获取cookie
        HttpCookie cookie = request.getCookies().getFirst(JwtConstants.COOKIE_NAME);
//        String auth = request.getHeaders().get("Authorization");Authorization
//        String token = HttpServletRequest.getHeader("Authorization");
//        ((ReactorServerHttpRequest) request).request.requestHeaders().get("Authorization").replace("Bearer ", "");


        if (cookie == null) {
            // 没有登录，放行
            return chain.filter(exchange);
        }
        // 3.校验token是否有效
        String jwt = cookie.getValue();
        try {
            // 3.1.解析并验证token
            Payload payload = jwtUtils.parseJwt(jwt);
            // 3.2.获取用户
            UserDetail userInfo = payload.getUserDetail();
            // 3.3.刷新jwt
            jwtUtils.refreshJwt(userInfo.getId());
            log.info("用户{}正在访问{}", userInfo.getUsername(), request.getURI().getPath());
        } catch (Exception e) {
            // 解析失败，token有误
            log.info("用户未登录");
        }
        // 5.放行
        return chain.filter(exchange);

    }

    // 该拦截最优先执行
    @Override
    public int getOrder() {
        // 登录拦截，可以采用最高优先级！
        return HIGHEST_PRECEDENCE;
    }

}