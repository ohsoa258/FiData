package com.fisk.datafactory.dto.dataaccess;

import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAccessIdDTO {
    @ApiModelProperty(value = "应用id")
    public long appId;
    @ApiModelProperty(value = "表id")
    public long tableId;
    /**
     * Timer driven OR CRON driven
     */
    @ApiModelProperty(value = "Timer driven OR CRON driven")
    public String syncMode;
    /**
     * 表达式 OR 秒
     */
    @ApiModelProperty(value = "表达式 OR 秒")
    public String expression;
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
     * 数据类别
     */
    @ApiModelProperty(value = "数据类别")
    public OlapTableEnum olapTableEnum;

    /**
     * 事实表名
     */
    @ApiModelProperty(value = "事实表名")
    public String factTableName;
}
