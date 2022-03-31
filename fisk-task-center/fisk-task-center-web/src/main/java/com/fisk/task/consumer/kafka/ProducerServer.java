package com.fisk.task.consumer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class ProducerServer {

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
        kafkaTemplate.send(topic, message).addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("failure");
            }

            @Override
            public void onSuccess(Object o) {
                System.out.println("success");
            }
        });
    }

}
