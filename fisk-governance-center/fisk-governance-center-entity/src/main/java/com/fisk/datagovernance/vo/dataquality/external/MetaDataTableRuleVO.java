package com.fisk.datagovernance.vo.dataquality.external;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 元数据表规则VO
 * @date 2023/7/31 13:32
 */
@Data
public class MetaDataTableRuleVO {
    /**
     * 架构名称
     */
    @ApiModelProperty(value = "架构名称")
    public String schemaName;

    /**
     * 表Id
     */
    @ApiModelProperty(value = "表Id")
    public String tableUnique;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 规则说明
     */
    @ApiModelProperty(value = "规则说明")
    public String ruleIllustrate;
}
