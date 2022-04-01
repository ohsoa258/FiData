package com.fisk.datamodel.dto.datadomain;

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
     * 3.业务域 4.业务流程  5.事实表
     */
    public Integer flag;
}
