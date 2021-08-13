package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_atomic_indicators")
@EqualsAndHashCode(callSuper = true)
public class AtomicIndicatorsPO extends BasePO {
    /**
     * 事实表id
     */
    public int factId;
    /**
     * 事实字段表id
     */
    public int factAttributeId;
    /**
     * 计算逻辑
     */
    public String calculationLogic;
    /**
     * 原子指标名称
     */
    public String indicatorsName;
    /**
     * 原子指标描述
     */
    public String indicatorsDes;

}
