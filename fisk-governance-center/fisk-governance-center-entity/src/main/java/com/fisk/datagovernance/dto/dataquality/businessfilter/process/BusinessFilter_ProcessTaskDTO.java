package com.fisk.datagovernance.dto.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessTaskDTO {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * tb_bizfilter_rule表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_rule表主键ID")
    public int ruleId;

    /**
     * tb_bizfilter_process_assembly表组件Code
     */
    @ApiModelProperty(value = "tb_bizfilter_process_assembly表组件Code")
    public int assemblyCode;

    /**
     * 任务code
     */
    @ApiModelProperty(value = "任务code")
    public String taskCode;

    /**
     * 父级任务code
     */
    @ApiModelProperty(value = "父级任务code")
    public String parentTaskCode;

    /**
     * 自定义描述
     */
    @ApiModelProperty(value = "自定义描述")
    public String customDescribe;

    /**
     * 任务状态：1 启用 0 禁用
     */
    @ApiModelProperty(value = "任务状态：1 启用 0 禁用")
    public int taskState;

    /**
     * 任务线路状态：1 满足 2 不满足 3 条件追加 4 初始
     */
    @ApiModelProperty(value = "任务线路状态：1 满足 2 不满足 3 条件追加 4 初始")
    public int taskLineState;

    /**
     * x轴坐标
     */
    @ApiModelProperty(value = "x轴坐标")
    public String xAxle;

    /**
     * y轴坐标
     */
    @ApiModelProperty(value = "y轴坐标")
    public String yAxle;

    /**
     * 触发器规则信息
     */
    @ApiModelProperty(value = "触发器规则信息")
    public BusinessFilter_ProcessTriggerDTO processTriggerInfo;

    /**
     * 表达式规则信息
     */
    @ApiModelProperty(value = "表达式规则信息")
    public BusinessFilter_ProcessExpressDTO processExpressInfo;

    /**
     * SQL脚本信息
     */
    @ApiModelProperty(value = "SQL脚本信息")
    public BusinessFilter_ProcessSqlScriptDTO processSqlScriptInfo;

    /**
     * 字段赋值规则信息
     */
    @ApiModelProperty(value = "字段赋值规则信息")
    public BusinessFilter_ProcessFieldAssignDTO processFieldAssignInfo;
}
