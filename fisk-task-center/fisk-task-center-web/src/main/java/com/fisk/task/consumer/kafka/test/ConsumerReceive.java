package com.fisk.task.consumer.kafka.test;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class ConsumerReceive {
    public static void main(String args[]) {

        //1.参数配置:不是每一非得配置
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.92:9092");
        props.put("auto.commit.interval.ms", "1000");
        //因为每一个消费者必须属于某一个消费者组，所以必须还设置group.id
        props.put("group.id", "groupidtestcfk");
        props.put("enable.auto.commit", "true");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        //2.创建消费者对象，并建立连接
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,String>(props);

        //3.设置从"my-topic"主题下拿取数据
        consumer.subscribe(Arrays.asList("testcfk"));

        //4.消费数据
        while (true) {
            //阻塞时间，从kafka中取出100毫秒的数据，有可能一次性去除0-n条
            ConsumerRecords<String, String> records = consumer.poll(100);
            //遍历
            for (ConsumerRecord<String, String> record : records)
                //打印结果
                //System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
                System.out.println("消费者消费的数据为："+record.value());
        }
    }
}
