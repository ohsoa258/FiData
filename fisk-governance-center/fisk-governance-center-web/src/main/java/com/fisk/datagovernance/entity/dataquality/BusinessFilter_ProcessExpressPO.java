package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

@Data
@TableName("tb_bizfilter_process_express")
public class BusinessFilter_ProcessExpressPO extends BasePO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    public int ruleId;

    /**
     * tb_bizfilter_process_task表task_code
     */
    public String taskCode;

    /**
     * 数据标识
     */
    public String dataCode;

    /**
     * 表达式规则之间逻辑关系
     */
    public String expressRuleRelation;

    /**
     * 表达式预览文本
     */
    public String expressPreviewText;

    /**
     * 表达式SQL
     */
    public String expressSql;

    /**
     * 自定义描述
     */
    public String customDescribe;
}
