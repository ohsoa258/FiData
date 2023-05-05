package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/1 15:07
 * 维度
 */
@Data
public class DimensionNameDTO {
    @ApiModelProperty(value = "维度Id")
    public Long dimensionId;

    @ApiModelProperty(value = "维度中文名")
    public String dimensionCnName;
    /**
     * 1数据接入  2数据建模
     */
    @ApiModelProperty(value = " 1数据接入  2数据建模")
    public Integer flag;
}
