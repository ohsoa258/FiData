package com.fisk.mdm.dto.attributeGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:39
 * @Version 1.0
 */
@Data
public class AttributeGroupDetailsAddDTO {

    @ApiModelProperty(value = "组id")
    @NotNull
    private Integer groupId;
    @ApiModelProperty(value = "属性")
    private List<AttributeQueryDTO> attributes;
}
