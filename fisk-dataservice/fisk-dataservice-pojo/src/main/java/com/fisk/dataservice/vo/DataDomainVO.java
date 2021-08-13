package com.fisk.dataservice.vo;

import com.fisk.dataservice.dto.BusinessProcessDTO;
import com.fisk.dataservice.dto.DimensionDTO;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:01
 * 一级业务域
 */
@Data
public class DataDomainVO {
    public String businessName;
    public List<DimensionDTO> dimensionList;
    public List<BusinessProcessDTO> businessProcessList;
}
