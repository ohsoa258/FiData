package com.fisk.task.utils;

import com.alibaba.fastjson.JSON;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.task.dto.MQBaseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author gy
 */
@Slf4j
@Service
public class KafkaTemplateHelper {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * producer 同步方式发送数据
     *
     * @param topic   topic名称
     * @param message producer发送的数据
     */
    public void sendMessageSync(String topic, String message) throws InterruptedException, ExecutionException, TimeoutException {
        kafkaTemplate.send(topic, message).get(10, TimeUnit.SECONDS);
    }

    /**
     * producer 异步方式发送数据
     *
     * @param topic   topic名称
     * @param message producer发送的数据
     */
    public void sendMessageAsync(String topic, String message) {
        try {
            MQBaseDTO data = JSON.parseObject(message, MQBaseDTO.class);
            data.traceId = MDCHelper.getTraceId();
        } catch (Exception ex) {
            log.error("解析Kafka消息失败，参数无法反序列化成MQBaseDTO对象", ex);
        }
        kafkaTemplate
                .send(topic, message)
                .addCallback(
                        new ListenableFutureCallback() {
                            @Override
                            public void onFailure(Throwable throwable) {
                                log.info("failure");
                            }

                            @Override
                            public void onSuccess(Object o) {
                                log.info("success");
                            }
                        });
    }

}
