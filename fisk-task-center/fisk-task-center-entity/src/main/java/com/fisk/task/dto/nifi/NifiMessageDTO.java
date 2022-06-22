package com.fisk.task.dto.nifi;

import com.fisk.task.dto.pipeline.NifiStageDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
public class NifiMessageDTO {
    public String topic;
    public String message;
    public String groupId;
    public String startTime;
    public String endTime;
    public String counts;
    public String nifiStageDTO;
    /*
     * 管道批次号
     * */
    public String pipelTraceId;

    /*
     * job批次号
     * */
    public String pipelJobTraceId;

    /*
     * task批次号
     * */
    public String pipelTaskTraceId;

    /*
     * stage批次号
     * */
    public String pipelStageTraceId;

}
