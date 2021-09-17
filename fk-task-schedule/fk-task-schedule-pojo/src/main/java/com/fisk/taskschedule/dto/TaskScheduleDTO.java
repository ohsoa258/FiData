package com.fisk.taskschedule.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TaskScheduleDTO {
    public int id;
    /**
     * 子节点id
     */
    public int jobId;
    /**
     * 父节点id
     */
    public int jobPid;
    /**
     * 同步方式
     */
    public String syncMode;
    /**
     * 表达式
     */
    public String expression;
    /**
     * 日志
     */
    public String msg;
    /**
     * tree标识
     */
    public int flag;
    /**
     * 0:实时  1:非实时
     */
    public int appType;
}
