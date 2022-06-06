package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/1 15:07
 * 维度
 */
@Data
public class DimensionNameDTO {
    public Long dimensionId;
    public String dimensionCnName;
    /**
     * 1数据接入  2数据建模
     */
    public Integer flag;
}
