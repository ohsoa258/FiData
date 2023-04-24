package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildPublishKafkaProcessorDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "Kafka中间件")
    public String KafkaBrokers;
    @ApiModelProperty(value = "KafkaKey")
    public String KafkaKey;
    @ApiModelProperty(value = "主题名称")
    public String TopicName;
    @ApiModelProperty(value = "使用事物")
    public String UseTransactions;
}
