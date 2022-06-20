package com.fisk.datagovernance.dto.dataquality.rule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 查询规则DTO
 * @date 2022/6/15 12:29
 */
@Data
public class QueryRuleDTO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id",required = true)
    public int dataSourceId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称",required = true)
    public String tableName;
}
