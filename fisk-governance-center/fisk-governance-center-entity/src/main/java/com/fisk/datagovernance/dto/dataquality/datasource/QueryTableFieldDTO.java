package com.fisk.datagovernance.dto.dataquality.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 查询表字段DTO
 * @date 2024/8/29 17:45
 */
@Data
public class QueryTableFieldDTO {
    @ApiModelProperty(value = "表节点Id字段值")
    public String id;

    @ApiModelProperty(value = "表节点labelRelName字段值")
    public String labelRelName;

    @ApiModelProperty(value = "表节点labelFramework字段值")
    public String labelFramework;

    @ApiModelProperty(value = "表节点externalTable字段值")
    public boolean externalTable;

    @ApiModelProperty(value = "表节点sourceType字段值")
    public int sourceType;

    @ApiModelProperty(value = "表节点sourceId字段值")
    public int sourceId;
}
