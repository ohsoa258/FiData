package com.fisk.datamanagement.dto.standards;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-20
 * @Description:
 */
@Data
public class StandardsBeCitedDTO {

    @ApiModelProperty(value = "Id")
    private Integer Id;

    @ApiModelProperty(value = "standardsId")
    private Integer standardsId;

    @ApiModelProperty(value = "数据源ID")
    public Integer dbId;

    @ApiModelProperty(value = "数据库名称")
    private String databaseName;

    @ApiModelProperty(value = "数据表名称")
    private String tableName;

    @ApiModelProperty(value = "表字段名称")
    private String fieldName;

    @ApiModelProperty(value = "数据源名称")
    private String datasourceName;
}
