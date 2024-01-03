package com.fisk.datamanagement.dto.standards;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-03
 * @Description:
 */
@Data
public class StandardsSourceQueryDTO {
    @ApiModelProperty(value = "数据库名称")
    private String databaseName;

    @ApiModelProperty(value = "数据表名称")
    private String tableName;

    @ApiModelProperty(value = "表字段名称")
    private String fieldName;
}
