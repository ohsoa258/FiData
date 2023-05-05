package com.fisk.chartvisual.dto.components;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/21 13:17
 */
@Data
public class ComponentsOptionDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "版本")
    private String version;

    @ApiModelProperty(value = "路径")
    private String path;

    @ApiModelProperty(value = "字段名")
    private String fileName;
}
