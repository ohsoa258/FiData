package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 11:23
 * 维度字段
 */
@Data
public class DimensionAttributeDTO {
    public Long dimensionAttributeId;
    public String dimensionFieldCnName;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
}
