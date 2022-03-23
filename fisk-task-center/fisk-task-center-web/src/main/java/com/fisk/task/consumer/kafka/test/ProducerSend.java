package com.fisk.task.consumer.kafka.test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Properties;
@Component
public class ProducerSend {
    public static void main(String args[]) {
        //1.参数配置：端口、缓冲内存、最大连接数、key序列化、value序列化等等（不是每一个非要配置）
        Properties props=new Properties();
        props.put("bootstrap.servers", "192.168.1.92:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        //2.创建生产者对象，并建立连接
        Producer<String, String> producer = new KafkaProducer<String,String>(props);

        try {
            //3.在my-topic主题下，发送消息
            for (int i = 0; i < 10; i++) {
                System.out.println(Integer.toString(i));
                producer.send(new ProducerRecord<String, String>("my-topic", Integer.toString(i), Integer.toString(i)));
                Thread.sleep(500);
            }
        }
        catch (Exception e)
        {
            System.out.println("ERROR");
        }

        //4.关闭
        producer.close();

    }
}
