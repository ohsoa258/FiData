package com.fisk.mdm.dto.attributelog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/6/14 16:36
 * @Version 1.0
 */
@Data
public class AttributeRollbackDTO {

    /**
     * 日志表id
     */
    @ApiModelProperty(value = "日志表id")
    private Integer id;

    /**
     * 属性id
     */
    @ApiModelProperty(value = "属性id")
    private Integer attributeId;
}
