package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_derived_indicators_attribute")
@EqualsAndHashCode(callSuper = true)
public class DerivedIndicatorsAttributePO extends BasePO {

    /**
     * 事实字段表id
     */
    public int factAttributeId;

}
