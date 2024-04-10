package com.fisk.datagovernance.util;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class KafkaTopicUtils {

    private final static Logger logger = LoggerFactory.getLogger(KafkaTopicUtils.class);

    public static long totalMessageCount(String brokerList,String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("group.id", "message_count_group");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            List<TopicPartition> tps = Optional.ofNullable(consumer.partitionsFor(topic))
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(info -> new TopicPartition(info.topic(), info.partition()))
                    .collect(Collectors.toList());
            Map<TopicPartition, Long> beginOffsets = consumer.beginningOffsets(tps);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(tps);

            long sum = tps.stream().mapToLong(tp -> endOffsets.get(tp) - beginOffsets.get(tp)).sum();
            System.out.println("topic {" + topic + "} 的消息总量为: => " + sum);
            return sum;
        }
    }

    public static void main(String[] args) {
        String brokerList = "192.168.11.130:9092"; // Kafka broker 地址
        String topic = "pipeline.supervision"; // 要统计消息数量的 topic

        long messageCount = totalMessageCount(brokerList, topic);
        System.out.println("Total message count for topic '" + topic + "': " + messageCount);
    }
}