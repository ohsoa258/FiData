package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildConsumeKafkaProcessorDTO extends BaseProcessorDTO {
    public String KafkaBrokers;
    public String TopicNames;
    public String GroupID;
}
