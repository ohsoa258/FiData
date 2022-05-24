package com.fisk.task.listener.redis;

import com.fisk.dataaccess.client.DataAccessClient;
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
    @Resource
    DataAccessClient dataAccessClient;

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
//        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
//        String expiredKey = message.toString();
//        System.out.println("即将调用的节点:" + expiredKey);
//        //用户key失效不做处理
//        if (!expiredKey.toLowerCase().contains("auth")) {
//            //此时,expiredKey就是即将要调用的节点,需要发消息topic_name就是expiredKey
//            String[] split = expiredKey.split("\\.");
//            String tableType = split[4];
//            int type = Integer.parseInt(tableType);
//            if (Objects.equals(type, OlapTableEnum.PHYSICS_API.getValue())) {
//                ApiImportDataDTO apiImportDataDTO = new ApiImportDataDTO();
//                apiImportDataDTO.workflowId = split[3];
//                apiImportDataDTO.appId = Long.parseLong(split[5]);
//                apiImportDataDTO.apiId = Long.parseLong(split[6]);
//                dataAccessClient.importData(apiImportDataDTO);
//            } else {
//                kafkaTemplateHelper.sendMessageAsync(expiredKey, "即将调用的节点");
//            }
//        }
    }
}