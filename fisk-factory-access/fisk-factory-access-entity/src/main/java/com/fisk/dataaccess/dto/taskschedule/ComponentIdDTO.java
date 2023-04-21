package com.fisk.dataaccess.dto.taskschedule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class ComponentIdDTO {
    /**
     * nifi流程回写的应用组件id
     */
    @ApiModelProperty(value = "nifi流程回写的应用组件id")
    public String appComponentId;
    /**
     * nifi流程回写的物理表组件
     */
    @ApiModelProperty(value = "nifi流程回写的物理表组件")
    public String tableComponentId;
    /**
     * 调度组件id
     */
    @ApiModelProperty(value = "调度组件id")
    public String schedulerComponentId;
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;
    /**
     * 物理表名
     */
    @ApiModelProperty(value = "物理表名")
    public String tableName;
}
