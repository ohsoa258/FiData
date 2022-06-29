package com.fisk.gateway.filters;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.dto.clientregister.ClientRegisterDTO;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.constants.TraceConstant;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.mdc.MDCHelper;
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
import java.util.List;


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

        // 前端请求开始，创建Trace对象，追踪请求链路信息
        String traceId = MDCHelper.setTraceId(),
                spanId = MDCHelper.setSpanId();

        // 1.获取Request对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 把traceId设置到http请求头中
        request.mutate().header(TraceConstant.HTTP_HEADER_TRACE, traceId).build();

        // 是否跳过登录验证
        if (hasSkipInterception(request, exchange)) {
            return chain.filter(exchange);
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
            Payload payload = null;
            try {
                // 3.2登录用户的token解析
                payload = jwtUtils.parseJwt(token);
                // 3.3获取用户信息
                UserDetail userInfo = payload.getUserDetail();
                // 报表永久token
                int permanentToken = 102;
                if (userInfo.getId() == permanentToken) {
                    // 不需要刷新token过期时间,直接放行
                    return chain.filter(exchange);
                    // 推送数据的请求路径判断
                    // 200928
                } else if (userInfo.getId() >= RedisTokenKey.DATA_SERVICE_TOKEN && userInfo.getId() <= RedisTokenKey.TOKEN_MAX) {
                    // 推送数据白名单
                    ResultEntity<Boolean> result = authClient.pushDataPathIsExists(request.getPath().value());
                    if (result.code == ResultEnum.SUCCESS.getCode() && result.data) {
                        // 不需要刷新token过期时间,直接放行
                        return chain.filter(exchange);
                        // 当前请求路径不在推送数据的白名单中
                    } else if (result.code == ResultEnum.SUCCESS.getCode() && !result.data) {
                        log.error("远程调用失败，方法名：【auth-service:pushDataPathIsExists】");
                        return buildResult(response, exchange, ResultEnum.TOKEN_EXCEPTION.getMsg());
                    }
                } else {
                    // 3.3.刷新jwt
                    jwtUtils.refreshJwt(userInfo.getId());
                }
                log.info("用户: {}, 正在访问: {}", userInfo.getUserAccount(), request.getURI().getPath());
            } catch (Exception e) {
                // 3.2客户端注册的token解析
                payload = jwtUtils.parseClientRegisterJwt(token);
                // 3.3获取用户信息
                UserDetail userInfo = payload.getUserDetail();
                // 3.4已注册的客户端名称
                ResultEntity<List<String>> clientRes = authClient.getClientInfoList();
                // 当前客户端信息
                ResultEntity<ClientRegisterDTO> authClientData = authClient.getData(userInfo.getId());
                // feign接口请求成功
                if (clientRes.code == ResultEnum.SUCCESS.getCode() && authClientData.code == ResultEnum.SUCCESS.getCode()) {
                    List<String> clientInfoList = clientRes.data;
                    // 当存在客户端且用户信息匹配上客户端信息时,放行
                    if (CollectionUtils.isNotEmpty(clientInfoList) && clientInfoList.contains(userInfo.getUserAccount())) {
                        ClientRegisterDTO clientRegisterDTO = authClientData.data;
                        if (clientRegisterDTO != null && clientRegisterDTO.valid) {
                            // 当前客户端没有禁用
                            // 不需要刷新token过期时间,直接放行
                            return chain.filter(exchange);
                        } else {
                            return buildResult(response, exchange, "当前token已禁用或删除,请联系管理员");
                        }
                    }
                    log.info("客户端: {}, 正在访问: {}", userInfo.getUserAccount(), request.getURI().getPath());
                } else { // 两种情况会执行到else: 1: feign接口调用失败, 2: 当前客户端已删除,auth模块执行catch
                    // 远程调用失败
                    return buildResult(response, exchange, authClientData.msg);
                }
            }
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

    /**
     * 是否需要跳过网关验证
     *
     * @param request  request
     * @param exchange exchange
     * @return 是否应该跳过
     */
    private boolean hasSkipInterception(ServerHttpRequest request, ServerWebExchange exchange) {
        // 判断是不是swagger
        if (request.getPath().value().contains(SystemConstants.GATEWAY_SWAGGER_WHITELIST)) {
            return true;
        }
        // 判断是不是websocket
        if (SystemConstants.WEBSOCKET.equals(exchange.getRequest().getHeaders().getUpgrade())) {
            return true;
        }

        // 判断是否在白名单中
        ResultEntity<Boolean> res = authClient.pathIsExists(request.getPath().value());
        if (res.code == ResultEnum.SUCCESS.getCode() && res.data) {
            return true;
        } else if (res.code != ResultEnum.SUCCESS.getCode()) {
            log.error("远程调用失败，方法名：【auth-service:pathIsExists】");
        }
        return false;
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
        res.code = ResultEnum.UNAUTHENTICATE.getCode();
        return response.bufferFactory().wrap(JSON.toJSONString(res).getBytes());
    }

}