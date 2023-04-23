package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/27 10:37
 * @Version 1.0
 */
@Data
public class ViwGroupDetailsNameDTO {

    @ApiModelProperty(value = "属性Id")
    private Integer attributeId;
    @ApiModelProperty(value = "别名")
    private String aliasName;
}
