package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.RuleTypeEnum;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/6/23 10:45
 * @Version 1.0
 */
@TableName("tb_code_rule")
@Data
public class CodeRulePO extends BasePO {

    /**
     * 规则组id
     */
    private Integer groupId;

    /**
     * 排序
     */
    @TableField(value = "`order`")
    private Integer order;

    /**
     * 规则类型
     */
    private RuleTypeEnum ruleType;

    /**
     * 常量值
     */
    private String constValue;

    /**
     * 属性id
     */
    private Integer attributeId;

    /**
     * 时间格式
     */
    private String dateFormat;
}
