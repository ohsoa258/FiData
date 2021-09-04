package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_derived_indicators_limited")
@EqualsAndHashCode(callSuper = true)
public class DerivedIndicatorsLimitedPO extends BasePO {

    /**
     * 业务限定id
     */
    public int businessLimitedId;

    /**
     * 派生指标id
     */
    public int indicatorsId;

    /**
     * 条件(AND、OR)
     */
    public String conditions;

}
