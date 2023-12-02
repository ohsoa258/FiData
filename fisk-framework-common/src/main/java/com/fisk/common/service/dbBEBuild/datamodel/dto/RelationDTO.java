package com.fisk.common.service.dbBEBuild.datamodel.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RelationDTO {

    @ApiModelProperty(value = "源表id")
    public Integer sourceEntityId;

    @ApiModelProperty(value = "目标表id")
    public Integer targetEntityId;

    @ApiModelProperty(value = "源表名")
    public String sourceTable;

    @ApiModelProperty(value = "源字段名称")
    public String sourceColumn;

    @ApiModelProperty(value = "目标表名")
    public String targetTable;

    @ApiModelProperty(value = "目标字段")
    public String targetColumn;

}
