package com.fisk.mdm.dto.attributeGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/18 17:11
 * @Version 1.0
 */
@Data
public class AttributeQueryDTO {

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体ID")
    private Integer entityId;

    /**
     * 属性id
     */
    @ApiModelProperty(value = "属性ID")
    private Integer attributeId;
}
