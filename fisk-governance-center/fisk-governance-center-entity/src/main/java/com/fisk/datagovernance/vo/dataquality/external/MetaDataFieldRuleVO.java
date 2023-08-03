package com.fisk.datagovernance.vo.dataquality.external;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 元数据字段规则VO
 * @date 2023/7/31 13:32
 */
@Data
public class MetaDataFieldRuleVO {
    /**
     * 字段Id
     */
    @ApiModelProperty(value = "字段Id")
    public String fieldUnique;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 规则说明
     */
    @ApiModelProperty(value = "规则说明")
    public String ruleIllustrate;
}
