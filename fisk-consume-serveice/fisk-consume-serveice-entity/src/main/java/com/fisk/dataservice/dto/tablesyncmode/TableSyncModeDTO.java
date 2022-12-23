package com.fisk.dataservice.dto.tablesyncmode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableSyncModeDTO {

    public long id;
    /**
     * 类型：1api服务 2表服务 3 文件服务
     */
    @ApiModelProperty(value = "类型：1api服务 2表服务 3 文件服务")
    public Integer type;

    /**
     * 类型表id
     */
    @ApiModelProperty(value = "类型id")
    public Integer typeTableId;

    /**
     * timer driver
     */
    @ApiModelProperty(value = "timer driver")
    public String timerDriver;

    /**
     * corn表达式
     */
    @ApiModelProperty(value = "corn表达式")
    public String cornExpression;

    /**
     * 单个数据流文件加载最大数据行
     */
    @ApiModelProperty(value = "单个数据流文件加载最大数据行")
    public Integer maxRowsPerFlowFile;

    /**
     * 单词从结果集中提取的最大数据行
     */
    @ApiModelProperty(value = "单词从结果集中提取的最大数据行")
    public Integer fetchSize;

    /**
     * 触发类型:1定时触发 2关联触发
     */
    @ApiModelProperty(value = "触发类型:1定时触发 2关联触发")
    public Integer triggerType;

    /**
     * 关联管道
     */
    @ApiModelProperty(value = "关联管道id")
    public Integer associatePipe;

    /**
     * 调度类型:1TIMER DRIVEN 2CRON DRIVEN
     */
    @ApiModelProperty(value = "调度类型:1TIMER DRIVEN 2CRON DRIVEN")
    public Integer scheduleType;

    /**
     * 自定义脚本执行前
     */
    @ApiModelProperty(value = "自定义脚本执行前")
    public String customScriptBefore;

    /**
     * 自定义脚本执行后
     */
    @ApiModelProperty(value = "自定义脚本执行后")
    public String customScriptAfter;

}
