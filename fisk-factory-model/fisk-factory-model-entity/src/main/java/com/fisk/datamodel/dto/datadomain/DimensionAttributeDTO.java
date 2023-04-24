package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 11:23
 * 维度字段
 */
@Data
public class DimensionAttributeDTO {

    @ApiModelProperty(value = "维度属性Id")
    public Long dimensionAttributeId;

    @ApiModelProperty(value = "维度字段中文名")
    public String dimensionFieldCnName;
    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
}
