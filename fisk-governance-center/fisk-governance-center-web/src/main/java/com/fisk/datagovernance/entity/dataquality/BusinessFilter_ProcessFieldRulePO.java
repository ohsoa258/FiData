package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

@Data
@TableName("tb_bizfilter_process_field_rule")
public class BusinessFilter_ProcessFieldRulePO extends BasePO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    public int ruleId;

    /**
     * tb_bizfilter_process_task表task_code
     */
    public String taskCode;

    /**
     * tb_bizfilter_process_express表数据标识
     * 或者
     * tb_bizfilter_process_field_assign表数据标识
     */
    public String fkDataCode;

    /**
     * 随机名称
     */
    public String randomName;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段符号
     */
    public String fieldSymbol;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段值
     */
    public String fieldValue;
}
