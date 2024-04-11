package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FieldsAssociatedMetricsOrMetaObjDTO {

    /**
     * 指标id或数据元id
     */
    @ApiModelProperty(value = "指标id或数据元id")
    private Integer id;

    /**
     * 指标名称或数据元名称
     */
    @ApiModelProperty(value = "指标名称或数据元名称")
    private String name;

    /**
     * 类型 0指标 1数据元
     */
    @ApiModelProperty(value = "类型 0指标 1数据元")
    private Integer type;



}
