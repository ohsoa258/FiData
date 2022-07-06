package com.fisk.datagovernance.vo.dataquality.rule;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import lombok.Data;


/**
 * @author dick
 * @version 1.0
 * @description 查询表检验规则
 * @date 2022/6/20 9:58
 */
@Data
public class TableRuleTempVO {

    public int ruleId;

    public String key;

    public String ruleValue;

    public String Type;

    public ModuleTypeEnum moduleType;
}
