package com.fisk.datamodel.dto.dimensionattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 2.5
 * @description 关联维度表字段id-下拉对象
 * @date 2022/6/6 17:30
 */
@Data
public class DimensionAttributeSelectDTO {

    @ApiModelProperty(value = "关联字段id")
    public Long id;

    @ApiModelProperty(value = "关联字段名称")
    public String dimensionFieldEnName;
}
