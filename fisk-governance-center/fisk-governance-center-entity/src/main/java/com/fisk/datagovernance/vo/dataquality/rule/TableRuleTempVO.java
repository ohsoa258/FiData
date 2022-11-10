package com.fisk.datagovernance.vo.dataquality.rule;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import lombok.Data;

import java.util.List;


/**
 * @author dick
 * @version 1.0
 * @description 查询表检验规则
 * @date 2022/6/20 9:58
 */
@Data
public class TableRuleTempVO {

    // 规则ID
    public long ruleId;

    // 规则名称
    public String ruleName;

    // 字段ID
    public String fieldUnique;

    // 字段名称
    public String fieldName;

    // 表字段规则
    public String tableFieldRule;

    // 规则类型 TABLE FIELD
    public String Type;

    // 模块类型
    public ModuleTypeEnum moduleType;

    // 模板类型
    public TemplateTypeEnum templateType;
}
