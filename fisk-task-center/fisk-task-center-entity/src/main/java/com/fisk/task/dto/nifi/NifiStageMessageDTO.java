package com.fisk.task.dto.nifi;

import com.fisk.task.dto.pipeline.NifiStageDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
public class NifiStageMessageDTO {
    public String topic;
    public String message;
    public String groupId;
    public Date startTime;
    public Date endTime;
    public Integer counts;
    public NifiStageDTO nifiStageDTO;

}
