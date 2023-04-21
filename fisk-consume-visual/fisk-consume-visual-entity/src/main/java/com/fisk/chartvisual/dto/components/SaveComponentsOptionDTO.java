package com.fisk.chartvisual.dto.components;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/3/21 13:17
 */
@Data
public class SaveComponentsOptionDTO {

    @ApiModelProperty(value = "id")
    @NotNull
    private Integer id;
    @ApiModelProperty(value = "组成ID")
    private Integer componentId;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "版本")
    private String version;

    @ApiModelProperty(value = "路径")
    private String path;

    @ApiModelProperty(value = "字段名称")
    private String fileName;
}
