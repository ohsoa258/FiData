package com.fisk.dataservice.dto;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 17:17
 * 业务过程 二级
 */
@Data
public class BusinessProcessDTO {

    public String businessProcessCnName;
    /**
     * 事实表 三级
     */
    public List<FactDTO> factList;
    /**
     * 派生指标 三级
     */
    public List<DerivedIndicatorsDTO> derivedIndicatorsList;
}
