package com.fisk.task.consumer.kafka;

import lombok.Data;

import java.util.Date;

@Data
public class KafkaReceiveDTO {
    public Integer tableId;
    public Integer tableType;
    public Integer numbers;
    public String topic;
    public Date endTime;


}
