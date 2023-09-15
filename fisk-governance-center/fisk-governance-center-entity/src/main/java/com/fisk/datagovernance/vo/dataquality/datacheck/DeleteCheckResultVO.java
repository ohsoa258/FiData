package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 删除校验结果VO
 * @date 2022/3/22 15:35
 */
@Data
public class DeleteCheckResultVO {
    /**
     * 数据校验规则id
     */
    @ApiModelProperty(value = "数据校验规则id")
    public int ruleId;

    /**
     * 数据校验规则名称
     */
    @ApiModelProperty(value = "数据校验规则名称")
    public String ruleName;

    /**
     * 错误数据保留天数
     */
    @ApiModelProperty(value = "错误数据保留天数")
    public int errorDataRetentionTime;
}
