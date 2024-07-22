package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验规则-检查条件
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_datacheck_rule_condition")
public class DataCheckConditionPO extends BasePO {
    /**
     * 数据校验规则id
     */
    public int ruleId;

    /**
     * 字段名称/字段Id
     */
    public String fieldUnique;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段类型
     */
    public String fieldOperator;

    /**
     * 字段类型
     */
    public String fieldValue;

    /**
     * 字段类型
     */
    public String fieldRelationCondition;
}
