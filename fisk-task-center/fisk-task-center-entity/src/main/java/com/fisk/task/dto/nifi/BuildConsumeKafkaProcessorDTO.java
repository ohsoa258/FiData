package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildConsumeKafkaProcessorDTO extends BaseProcessorDTO {
    public String kafkaBrokers;
    public String topicNames;
    public String GroupId;
    public boolean honorTransactions;
}
