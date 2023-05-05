package com.fisk.datamodel.dto.dimensionattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionMetaDTO {
    /**
     * 维度id
     */
    @ApiModelProperty(value = "维度id")
    public long id;
    /**
     * 维度表名
     */
    @ApiModelProperty(value = "维度表名")
    public String dimensionTabName;
}
