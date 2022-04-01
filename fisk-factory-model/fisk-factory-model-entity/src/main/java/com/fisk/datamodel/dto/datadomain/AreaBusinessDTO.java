package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/19 10:16
 * 一级业务域
 */
@Data
public class AreaBusinessDTO {
    public Long businessId;
    public String businessName;
    public List<DimensionDTO> dimensionList;
    public List<BusinessProcessDTO> businessProcessList;
}
