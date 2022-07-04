package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/6/23 10:45
 * @Version 1.0
 */
@TableName("tb_code_rule_group")
@Data
public class CodeRuleGroupPO extends BasePO {

    /**
     * 实体id
     */
    private Integer entityId;

    /**
     * 属性id
     */
    private Integer attributeId;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    @TableField(value = "`desc`")
    private String desc;
}
