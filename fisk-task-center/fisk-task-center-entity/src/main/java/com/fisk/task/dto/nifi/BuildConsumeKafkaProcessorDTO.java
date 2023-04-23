package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildConsumeKafkaProcessorDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "kafka中间件")
    public String kafkaBrokers;
    @ApiModelProperty(value = "主题名称")
    public String topicNames;
    @ApiModelProperty(value = "组Id")
    public String GroupId;
    @ApiModelProperty(value = "荣誉事务")
    public boolean honorTransactions;
}
