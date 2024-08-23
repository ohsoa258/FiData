package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则组
 * @date 2024/8/23 10:09
 */
@Data
public class DataCheckRuleGroupDTO {
    /**
     * 数据元标准ID集合(数据标准)
     */
    @ApiModelProperty(value = "数据元标准ID集合(数据标准)")
    public List<Integer> standardIdList;
}
