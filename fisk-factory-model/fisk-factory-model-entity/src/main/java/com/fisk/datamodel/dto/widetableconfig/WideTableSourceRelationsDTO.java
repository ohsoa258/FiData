package com.fisk.datamodel.dto.widetableconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableSourceRelationsDTO {

    @ApiModelProperty(value = "源表名")
    public String sourceTable;

    @ApiModelProperty(value = "源字段名称")
    public String sourceColumn;

    @ApiModelProperty(value = "连接类型: left join, join, right join...")
    public String joinType;

    @ApiModelProperty(value = "目标表名")
    public String targetTable;

    @ApiModelProperty(value = "目标字段")
    public String targetColumn;

}
