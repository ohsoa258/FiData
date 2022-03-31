package com.fisk.task.dto.daconfig;

import lombok.Data;

/**
 * @author gy
 */
@Data
public class TaskGroupConfig {
    public long id;
    public String appName;

    public String appDetails;

    /**
     * 组件id
     */
    public String componentId;

    /*
    * 是否是新的任务组
    * */
    public Boolean isNewTask;
}
