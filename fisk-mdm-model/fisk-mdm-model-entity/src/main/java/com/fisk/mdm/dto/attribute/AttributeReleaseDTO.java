package com.fisk.mdm.dto.attribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/22 16:54
 * @Version 1.0
 */
@Data
public class AttributeReleaseDTO {

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String desc;
    /**
     * 属性日志表id
     */
    @ApiModelProperty(value = "属性日志表id")
    private Integer attributeLogId;
}
