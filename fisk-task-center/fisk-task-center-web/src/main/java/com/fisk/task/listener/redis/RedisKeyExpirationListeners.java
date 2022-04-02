package com.fisk.task.listener.redis;

import com.fisk.task.utils.KafkaTemplateHelper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisKeyExpirationListeners extends KeyExpirationEventMessageListener {
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;

    public RedisKeyExpirationListeners(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
        String expiredKey = message.toString();
        System.out.println("即将调用的节点:" + expiredKey);
        //此时,expiredKey就是即将要调用的节点,需要发消息topic_name就是expiredKey
        kafkaTemplateHelper.sendMessageAsync(expiredKey, "即将调用的节点");
    }
}