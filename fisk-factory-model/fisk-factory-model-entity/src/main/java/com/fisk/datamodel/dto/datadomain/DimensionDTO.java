package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:10
 * 维度
 */
@Data
public class DimensionDTO {
    @ApiModelProperty(value = "维度Id")
    public Long dimensionId;
    @ApiModelProperty(value = "维度表名")
    public String dimensionTabName;
    @ApiModelProperty(value = "维度属性列表")
    public List<DimensionAttributeDTO> dimensionAttributeList;
    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
}
