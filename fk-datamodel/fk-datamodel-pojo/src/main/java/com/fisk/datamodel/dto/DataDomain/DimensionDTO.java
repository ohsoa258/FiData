package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:10
 * 维度
 */
@Data
public class DimensionDTO {
    public Long dimensionId;
    public String dimensionTabName;
    public List<DimensionAttributeDTO> dimensionAttributeList;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
}
