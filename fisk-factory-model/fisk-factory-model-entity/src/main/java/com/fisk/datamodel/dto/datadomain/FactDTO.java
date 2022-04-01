package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/16 15:06
 * 事实表 三级
 */
@Data
public class FactDTO {
    public Long factId;
    public String factTabName;
    /**
     * 原子指标 三级
     */
    public List<AtomicIndicatorsDTO> atomicIndicatorsList;
    /**
     * 派生指标 三级
     */
    public List<DerivedIndicatorsDTO> derivedIndicatorsList;

    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
}
