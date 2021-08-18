package com.fisk.dataservice.dto;

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
    public String dimensionCnName;
    public List<DimensionAttributeDTO> dimensionAttributeList;
}
