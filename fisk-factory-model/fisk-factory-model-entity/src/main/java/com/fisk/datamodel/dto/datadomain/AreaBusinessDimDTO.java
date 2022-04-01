package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/9/10 14:36
 * 业务域  一级
 */
@Data
public class AreaBusinessDimDTO {
    public Long businessId;
    public String businessName;
    public List<DimensionDimDTO> dimensionList;
    public List<BusinessProcessDimDTO> businessProcessList;
    /**
     *  7.业务域  8.维度  9.业务过程  10.事实表
     */
    public Integer flag;
}
