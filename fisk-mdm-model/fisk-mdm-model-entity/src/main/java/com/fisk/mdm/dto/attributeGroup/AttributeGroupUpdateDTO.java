package com.fisk.mdm.dto.attributeGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:46
 * @Version 1.0
 */
@Data
public class AttributeGroupUpdateDTO {

    @ApiModelProperty(value = "id")
    @NotNull
    private Integer id;
    @ApiModelProperty(value = "模型id")
    private Integer modelId;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "详细信息")
    private String details;
}
