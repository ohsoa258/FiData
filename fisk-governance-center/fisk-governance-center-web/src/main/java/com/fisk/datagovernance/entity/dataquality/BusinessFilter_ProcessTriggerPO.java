package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

@Data
@TableName("tb_bizfilter_process_trigger")
public class BusinessFilter_ProcessTriggerPO extends BasePO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    public int ruleId;

    /**
     * tb_bizfilter_process_task表task_code
     */
    public String taskCode;

    /**
     * 触发场景：1 调度任务 2 质量报告
     */
    public int triggerScene;

    /**
     * 调度类型：TIMER DRIVEN、CRON DRIVEN
     */
    public String triggerType;

    /**
     * 调度周期
     */
    public String triggerValue;

    /**
     * 自定义描述
     */
    public String customDescribe;
}
