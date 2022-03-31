package com.fisk.dataaccess.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiAccessDTO {

    /**
     * 物理表id
     */
    public long tableId;

    /**
     * 应用注册id
     */
    public long appId;

    /**
     * app组GroupConfig componentId
     */
    public String appGroupId;

    /**
     * 任务组TaskGroupConfig componentId
     */
    public String tableGroupId;

    /**
     * targetDbPoolComponentId
     */
    public String targetDbPoolComponentId;

    /**
     * sourceDbPoolComponentId
     */
    public String sourceDbPoolComponentId;

    /**
     * cfgDbPoolComponentId
     */
    public String cfgDbPoolComponentId;

    /**
     * 调度组件id
     */
    public String schedulerComponentId;
}
