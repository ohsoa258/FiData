package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:21
 * 原子指标 四级
 */
@Data
public class AtomicIndicatorsDTO {

    public Long indicatorsId;

    public String indicatorsName;

    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
}
