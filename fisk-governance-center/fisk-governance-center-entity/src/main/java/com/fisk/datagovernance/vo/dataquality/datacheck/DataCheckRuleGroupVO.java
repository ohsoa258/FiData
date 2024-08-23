package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则组
 * @date 2024/8/23 10:05
 */
@Data
public class DataCheckRuleGroupVO {
    /**
     * 数据元标准ID(数据标准)
     */
    @ApiModelProperty(value = "数据元标准ID(数据标准)")
    public Integer standardsId;

    /**
     * 规则所属分组id(数据标准)
     */
    @ApiModelProperty(value = "规则所属分组id(数据标准)")
    public Integer dataCheckGroupId;

    /**
     * 规则所属分组名称(数据标准)
     */
    @ApiModelProperty(value = "规则所属分组名称(数据标准)")
    public String checkGroupName;
}
