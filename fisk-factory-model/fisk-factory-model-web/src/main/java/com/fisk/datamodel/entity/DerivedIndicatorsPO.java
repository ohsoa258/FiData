package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_derived_indicators")
@EqualsAndHashCode(callSuper = true)
public class DerivedIndicatorsPO extends BasePO {

    /**
     * 派生指标名称
     */
    public String derivedName;

    /**
     * 派生指标描述
     */
    public String derivedDes;

    /**
     * 原子指标id
     */
    public long atomicId;

    /**
     * 时间周期
     */
    public String timePeriod;

    /**
     * 事实表id
     */
    public long factId;

}
