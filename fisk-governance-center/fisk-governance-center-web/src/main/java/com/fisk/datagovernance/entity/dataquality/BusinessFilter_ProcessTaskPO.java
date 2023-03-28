package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

@Data
@TableName("tb_bizfilter_process_task")
public class BusinessFilter_ProcessTaskPO extends BasePO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    public int ruleId;

    /**
     * tb_bizfilter_process_assembly表主键ID
     */
    public int assemblyId;

    /**
     * 任务code
     */
    public String taskCode;

    /**
     * 父级任务code
     */
    public String parentTaskCode;

    /**
     * 自定义描述
     */
    public String customDescribe;

    /**
     * 任务状态：1 启用 0 禁用
     */
    public int taskState;

    /**
     * 任务线路状态：1 满足 2 不满足 3 条件追加 4 初始
     */
    public int taskLineState;

    /**
     * x轴坐标
     */
    public String xAxle;

    /**
     * y轴坐标
     */
    public String yAxle;
}
