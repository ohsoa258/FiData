package com.fisk.datafactory.dto.components;

/**
 * @author cfk
 */
public class TableUsageDTO {
    /**
     * 管道id
     */
    public long pipelId;
    /**
     * 管道名称
     */
    public String pipelName;
    /**
     * 组id
     */
    public long jobId;
    /**
     * 组名称
     */
    public String jobName;
    /**
     * 任务id
     */
    public long taskId;
    /**
     * 任务名称
     */
    public String taskName;
    /**
     * 表所在组第几个
     */
    public long tableOrder;


}
