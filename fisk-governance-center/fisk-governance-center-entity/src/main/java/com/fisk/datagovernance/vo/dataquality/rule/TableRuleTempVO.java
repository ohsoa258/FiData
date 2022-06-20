package com.fisk.datagovernance.vo.dataquality.rule;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
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
