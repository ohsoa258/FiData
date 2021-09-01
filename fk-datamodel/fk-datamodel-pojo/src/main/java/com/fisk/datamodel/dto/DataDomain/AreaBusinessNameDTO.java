package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/19 10:16
 * 一级业务域
 */
@Data
public class AreaBusinessNameDTO {
    public Long businessId;
    public String businessName;
    public List<BusinessProcessNameDTO> businessProcessList;
    /**
     * 1数据接入  2数据建模
     */
    public Integer flag;
}
