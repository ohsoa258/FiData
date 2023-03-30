package com.fisk.datagovernance.vo.dataquality.businessfilter.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BusinessFilter_ProcessTriggerVO {
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
     * tb_bizfilter_process_task表task_code
     */
    @ApiModelProperty(value = "tb_bizfilter_process_task表task_code")
    public String taskCode;

    /**
     * 触发场景：1 调度任务 2 质量报告
     */
    @ApiModelProperty(value = "触发场景：1 调度任务 2 质量报告")
    public int triggerScene;

    /**
     * 调度类型：TIMER DRIVEN、CRON DRIVEN
     */
    @ApiModelProperty(value = "调度类型：TIMER DRIVEN、CRON DRIVEN")
    public String triggerType;

    /**
     * 调度周期
     */
    @ApiModelProperty(value = "调度周期")
    public String triggerValue;

    /**
     * 自定义描述
     */
    @ApiModelProperty(value = "自定义描述")
    public String customDescribe;
}
