package com.fisk.gateway.filters;

import com.alibaba.fastjson.JSON;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.common.constants.SystemConstants;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;


/**
 * @author Lock
 * @date 2021/5/17 17:16
 * 拦截器: 用于拦截用户请求,并刷新有效期
 */
@Slf4j
@Component
public class LoginFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtils jwtUtils;
    @Resource
    AuthClient authClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.获取Request对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 判断是不是swagger
        if (request.getPath().value().contains(SystemConstants.GATEWAY_SWAGGER_WHITELIST)) {
            return chain.filter(exchange);
        }
        // 判断是不是websocket
        if (SystemConstants.WEBSOCKET.equals(exchange.getRequest().getHeaders().getUpgrade())) {
            return chain.filter(exchange);
        }
        // 判断是否在白名单中
        ResultEntity<Boolean> res = authClient.pathIsExists(request.getPath().value());
        if (res.code == ResultEnum.SUCCESS.getCode() && res.data) {
            return chain.filter(exchange);
        } else if (res.code != ResultEnum.SUCCESS.getCode()) {
            log.error("远程调用失败，方法名：【auth-service:pathIsExists】");
            return buildResult(response, exchange, ResultEnum.REMOTE_SERVICE_CALLFAILED.getMsg());
        }

        // 2. 获取token
        String token = request.getHeaders().getFirst(SystemConstants.HTTP_HEADER_AUTH);
        if (StringUtils.isEmpty(token)) {
            return buildResult(response, exchange, "没有token，请先登录");
        }
        token = token.replace(SystemConstants.AUTH_TOKEN_HEADER, "");

        // 3.校验token是否有效
        try {
            // 3.1.解析并验证token
            Payload payload = jwtUtils.parseJwt(token);
            // 3.2.获取用户
            UserDetail userInfo = payload.getUserDetail();
            // 3.3.刷新jwt
            jwtUtils.refreshJwt(userInfo.getId());
            log.info("用户{}正在访问{}", userInfo.getUsername(), request.getURI().getPath());
        } catch (Exception e) {
            // 解析失败，token有误
            log.info(e.getMessage());
            return buildResult(response, exchange, e.getMessage());
        }
        // 5.放行
        return chain.filter(exchange);
    }

    /**
     * 该拦截最优先执行
     *
     * @return 级别
     */
    @Override
    public int getOrder() {
        // 登录拦截，可以采用最高优先级！
        return HIGHEST_PRECEDENCE;
    }

    private Mono<Void> buildResult(ServerHttpResponse response, ServerWebExchange exchange, String str) {
        return response.writeWith(Mono.just(buildMessage(response, str)));
    }

    private static String convertHttpToWs(String scheme) {
        scheme = scheme.toLowerCase();
        return "http".equals(scheme) ? "ws" : "https".equals(scheme) ? "wss" : scheme;
    }

    private DataBuffer buildMessage(ServerHttpResponse response, String str) {
        //设置headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        ResultEntity<Object> res = new ResultEntity<>();
        res.msg = str;
        res.code = ResultEnum.UNAUTHORIZED.getCode();
        return response.bufferFactory().wrap(JSON.toJSONString(res).getBytes());
    }

}