package com.fisk.dataaccess.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiAccessDTO {

    /**
     * 物理表id
     */
    @ApiModelProperty(value = "物理表id")
    public long tableId;

    /**
     * 应用注册id
     */
    @ApiModelProperty(value = "应用注册id")
    public long appId;

    /**
     * app组GroupConfig componentId
     */
    @ApiModelProperty(value = " app组GroupConfig componentId")
    public String appGroupId;

    /**
     * 任务组TaskGroupConfig componentId
     */
    @ApiModelProperty(value = "任务组TaskGroupConfig componentId")
    public String tableGroupId;

    /**
     * targetDbPoolComponentId
     */
    @ApiModelProperty(value = "targetDbPoolComponentId")
    public String targetDbPoolComponentId;

    /**
     * sourceDbPoolComponentId
     */
    @ApiModelProperty(value = "sourceDbPoolComponentId")
    public String sourceDbPoolComponentId;

    /**
     * cfgDbPoolComponentId
     */
    @ApiModelProperty(value = "cfgDbPoolComponentId")
    public String cfgDbPoolComponentId;

    /**
     * 调度组件id
     */
    @ApiModelProperty(value = "调度组件id")
    public String schedulerComponentId;
}
