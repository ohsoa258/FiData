package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:23
 * 派生指标
 */
@Data
public class DerivedIndicatorsDTO {

    public Long indicatorsId;

    public String derivedName;

    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;
}
