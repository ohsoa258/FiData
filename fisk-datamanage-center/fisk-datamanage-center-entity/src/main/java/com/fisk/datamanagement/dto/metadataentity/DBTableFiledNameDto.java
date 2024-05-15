package com.fisk.datamanagement.dto.metadataentity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class DBTableFiledNameDto {
    @ApiModelProperty(value = "数据库名称")
    private String databaseName;

    @ApiModelProperty(value = "数据表名称")
    private String tableName;

    @ApiModelProperty(value = "表字段名称")
    private String fieldName;

    @ApiModelProperty(value = "数据库id")
    private Integer dbId;

    @ApiModelProperty(value = "数据表id")
    private Integer tbId;

    @ApiModelProperty(value = "表字段id")
    private Integer fieldId;
}
