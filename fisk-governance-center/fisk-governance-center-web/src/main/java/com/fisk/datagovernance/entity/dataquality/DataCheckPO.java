package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验组件表
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_datacheck_module")
public class DataCheckPO extends BasePO {
    /**
     * 模板id
     */
    public int templateId;

    /**
     * 数据源id
     */
    public int datasourceId;

    /**
     * 数据源类型
     */
    public int datasourceType;

    /**
     * 组件名称
     */
    public String moduleName;

    /**
     * 检验步骤
     */
    public int checkStep;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 前置表名称
     */
    public String proTableName;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段聚合函数
     */
    public String fieldAggregate;

    /**
     * 校验规则的类型
     */
    public String checkRuleType;

    /**
     * 波动阈值
     */
    public Integer thresholdValue;

    /**
     * 表行数，实际表行数减去表行数
     */
    public Integer rowsValue;

    /**
     * 运行时间表达式
     */
    public String runTimeCron;

    /**
     * 上下游血缘关系范围
     */
    public Integer checkConsanguinity;

    /**
     * 组件规则类型
     */
    public int moduleType;

    /**
     * 组件规则
     */
    public String moduleRule;

    /**
     * 组件状态
     */
    public int moduleState;
}


