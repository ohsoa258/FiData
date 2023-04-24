package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/10 14:39
 * 维度 二级
 */
@Data
public class DimensionDimDTO {
    @ApiModelProperty(value = "维度Id")
    public Long dimensionId;
    @ApiModelProperty(value = "维度中文名")
    public String dimensionCnName;
    /**
     * 7.业务域  8.维度  9.业务过程  10.事实表
     */
    @ApiModelProperty(value = "7.业务域  8.维度  9.业务过程  10.事实表")
    public Integer flag;
    /**
     * 上一级id
     */
    @ApiModelProperty(value = "上一级id")
    public Long pid;
}
