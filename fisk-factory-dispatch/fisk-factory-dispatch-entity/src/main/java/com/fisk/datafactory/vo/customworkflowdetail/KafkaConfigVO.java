package com.fisk.datafactory.vo.customworkflowdetail;

import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-11-04
 * @Description:
 */
@Data
public class KafkaConfigVO {
    public String kafkaBroker;
    public String topic;
    public String value;
}
