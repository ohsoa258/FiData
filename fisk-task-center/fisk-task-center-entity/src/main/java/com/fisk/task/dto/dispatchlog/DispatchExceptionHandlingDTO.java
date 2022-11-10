package com.fisk.task.dto.dispatchlog;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class DispatchExceptionHandlingDTO {
    /**
     * 管道批次id
     */
    public String pipelTraceId;
    /**
     * job批次号
     */
    public String pipelJobTraceId;

    /**
     * task批次号
     */
    public String pipelTaskTraceId;

    /**
     * stage批次号
     */
    public String pipelStageTraceId;

    /**
     * 报错日志
     */
    public String comment;

    /**
     * 管道名称
     */
    public String pipleName;

    /**
     * job名称
     */
    public String JobName;


}
