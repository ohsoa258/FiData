package com.fisk.task.utils;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.MessageLevelEnum;
import com.fisk.common.core.enums.task.MessageStatusEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.task.entity.MessageLogPO;
import com.fisk.task.mapper.MessageLogMapper;
import com.fisk.task.vo.WsMessageLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gy
 */
@Slf4j
@Component
public class WsSessionManager {

    @Resource
    MessageLogMapper mapper;

    static MessageLogMapper mapperService;

    @PostConstruct
    public void init() {
        mapperService = mapper;
    }


    /**
     * 保存连接 session 的地方
     */
    private static ConcurrentHashMap<Long, Session> SESSION_POOL = new ConcurrentHashMap<>();
    /**
     * 记录当前在线连接数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 添加 session
     *
     * @param key key
     */
    public static void add(Long key, Session session) {
        // 添加 session
        SESSION_POOL.put(key, session);
        onlineCount.incrementAndGet();
    }

    /**
     * 删除 session,会返回删除的 session
     *
     * @param key key
     * @return 删除的连接
     */
    public static Session remove(Long key) {
        Session res = SESSION_POOL.remove(key);
        if(res != null) {
            onlineCount.decrementAndGet();
        }
        // 删除 session
        return res;
    }

    /**
     * 删除并同步关闭连接
     *
     * @param key key
     */
    public static void removeAndClose(Long key) {
        Session session = remove(key);
        if (session != null) {
            try {
                // 关闭连接
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("ws连接关闭失败：", e);
            }
        }
    }

    /**
     * 获得 session
     *
     * @param key key
     * @return 连接
     */
    public static Session get(Long key) {
        // 获得 session
        return SESSION_POOL.get(key);
    }

    /**
     * 获取在线人数
     *
     * @return 在线人数
     */
    public static int getOnlineCount() {
        return onlineCount.get();
    }

    /**
     * 根据id发送消息
     */
    public static void sendMsgById(String message, Long id, MessageLevelEnum level) {
        Session session = SESSION_POOL.get(id);
        sendMsg(session, message, id, level);
    }

    /**
     * 群发消息
     *
     * @param message 消息内容
     * @param id      发送者id(发送时，排除次id)
     */
    public static void sendMsgByAll(String message, Long id, MessageLevelEnum level) {
        for (Map.Entry<Long, Session> sessionEntry : SESSION_POOL.entrySet()) {
            //排除掉自己
            if (!sessionEntry.getKey().equals(id)) {
                sendMsg(sessionEntry.getValue(), message, id, level);
            }
        }
    }

    /**
     * 群发消息
     *
     * @param message 消息内容
     * @param ids     接收者id
     */
    public static void sendMsgByAll(String message, List<Long> ids, MessageLevelEnum level) {
        for (Long id : ids) {
            Session session = SESSION_POOL.get(id);
            if (session != null) {
                sendMsg(session, message, id, level);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param session session
     * @param msg     msg
     */
    public static void sendMsgBySession(String msg, Session session, Long id, MessageLevelEnum level) {
        sendMsg(session, msg, id, level);
    }

    /**
     * 发送消息
     *
     * @param session session
     * @param msg     msg
     */
    public static void sendMsgBySession(String msg, Session session, MessageLevelEnum level) {
        sendMsg(session, msg, 0L, level);
    }

    /**
     * 发送消息
     *
     * @param session session
     * @param msg     msg
     */
    private static void sendMsg(Session session, String msg, Long id, MessageLevelEnum level) {
        MDCHelper.setAppLogType(TraceTypeEnum.TASK_WS_SEND_MESSAGE);

        //记录日志
        MessageLogPO model = new MessageLogPO();
        model.createUser = id.toString();
        model.status = MessageStatusEnum.UNREAD;
        model.level = level;
        model.msg = msg;
        mapperService.insert(model);

        //低等级的不需要发消息，只落表
        if(level != MessageLevelEnum.LOW) {
            //发送消息
            WsMessageLogVO vo = new WsMessageLogVO();
            vo.msg = msg;
            vo.id = model.id;
            vo.status = model.status;
            vo.createTime = model.createTime;
            vo.level = level;

            try {
                if (session != null) {
                    session.getBasicRemote().sendText(JSON.toJSONString(vo));
                    log.info("ws消息发送成功，接收者id【{}】，发送时间【{}】，发送内容【{}】", id, DateTimeUtils.getNow(), msg);
                }
            } catch (Exception e) {
                log.error("ws消息发送失败，接收者id【" + id + "】，发送时间【" + DateTimeUtils.getNow() + "】，错误信息：", e);
            }
        }
        MDCHelper.removeLogType();
    }
}
