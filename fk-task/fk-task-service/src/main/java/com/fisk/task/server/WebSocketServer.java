package com.fisk.task.server;

import com.fisk.common.constants.SystemConstants;
import com.fisk.common.enums.task.MessageLevelEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.common.utils.DateTimeUtils;
import com.fisk.task.utils.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URLDecoder;
import java.time.LocalDateTime;

/**
 * @author gy
 */
@Slf4j
@ServerEndpoint(value = "/ws/server/{token}")
@Component
public class WebSocketServer {

    private static ApplicationContext context;
    private UserHelper userHelper;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws Exception {
        token = parseToken(token);
        userHelper = context.getBean(UserHelper.class);
        RedisUtil bean = context.getBean(RedisUtil.class);
        ResultEntity<UserInfo> res = userHelper.getLoginUserInfo(token);
        String msg = "【" + DateTimeUtils.getNow() + "】";
        if (res.code == ResultEnum.SUCCESS.getCode()) {
            msg += "连接成功";
            log.info("有新连接加入：{}，当前在线人数为：{}", token, WsSessionManager.getOnlineCount());
            WsSessionManager.add(res.data.id, session);
            WsSessionManager.sendMsgBySession(msg, session, res.data.id, MessageLevelEnum.LOW);
        } else {
            msg += res.msg;
            UserInfo one = bean.getOne("Bearer "+token);
            WsSessionManager.add(one.id, session);
            WsSessionManager.sendMsgBySession(msg, session, MessageLevelEnum.LOW);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("token") String token) throws Exception {
        token = parseToken(token);
        Long id = userHelper.getUserIdByToken(token);
        if (id != null) {
            WsSessionManager.remove(id);
            log.info("连接关闭：{}，当前在线人数为：{}", token, WsSessionManager.getOnlineCount());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("token") String token) throws Exception {
        token = parseToken(token);
        /*RedisUtil bean = context.getBean(RedisUtil.class);
        UserInfo one = bean.getOne("Bearer "+token);
        log.info("当前用户token为"+token+",查到的用户信息为:"+one);
        WsSessionManager.sendMsgBySession(msg, session, one.id, MessageLevelEnum.LOW);*/
        log.info("服务端收到客户端[{}]的消息:{}", token, message);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("token") String token) throws Exception {
        token = parseToken(token);
        log.error("用户错误:" + token + ", 原因:", error);
        error.printStackTrace();
    }

    private String parseToken(String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            throw new FkException(ResultEnum.AUTH_TOKEN_IS_NOTNULL);
        }
        //网关路由过来的请求，token会被url编码，直接访问服务不会被编码
        token = URLDecoder.decode(token, "UTF-8");
        return token.replace(SystemConstants.AUTH_TOKEN_HEADER, "");
    }
}
