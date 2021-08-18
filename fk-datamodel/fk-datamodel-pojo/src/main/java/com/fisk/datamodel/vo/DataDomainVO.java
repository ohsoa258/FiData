package com.fisk.datamodel.vo;

import com.fisk.datamodel.dto.DataDomain.DimensionDTO;
import com.fisk.datamodel.dto.DataDomain.BusinessProcessDTO;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:01
 * 一级业务域
 */
@Data
public class DataDomainVO {
    public Long businessId;
    public String businessName;
    public List<DimensionDTO> dimensionList;
    public List<BusinessProcessDTO> businessProcessList;
}
