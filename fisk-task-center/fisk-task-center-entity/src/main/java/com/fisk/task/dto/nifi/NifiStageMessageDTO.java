package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class NifiStageMessageDTO {
    public String topic;
    public String message;
    public String groupId;
}
