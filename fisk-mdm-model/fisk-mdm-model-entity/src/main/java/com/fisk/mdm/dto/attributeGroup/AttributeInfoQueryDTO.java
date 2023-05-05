package com.fisk.mdm.dto.attributeGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/26 10:02
 * @Version 1.0
 */
@Data
public class AttributeInfoQueryDTO {

    /**
     * 属性组id
     */
    @ApiModelProperty(value = "属性组ID")
    private Integer groupId;

    /**
     * 模型id
     */
    @ApiModelProperty(value = "模型ID")
    private Integer modelId;
}
