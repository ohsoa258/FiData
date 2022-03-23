package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildPublishKafkaProcessorDTO extends BaseProcessorDTO {
    public String KafkaBrokers;
    public String KafkaKey;
    public String TopicName;
    public String UseTransactions;
}
