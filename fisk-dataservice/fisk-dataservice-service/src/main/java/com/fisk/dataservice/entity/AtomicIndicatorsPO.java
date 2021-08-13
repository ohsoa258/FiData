package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:36
 */
@Data
@TableName("tb_atomic_indicators")
public class AtomicIndicatorsPO extends BasePO {

    /**
     * 事实表id
     */
    private Integer factId;
    /**
     * 事实字段表id
     */
    private Integer factAttributeId;
    /**
     * 计算逻辑
     */
    private String calculationLogic;
    /**
     * 原子指标名称
     */
    private String indicatorsName;
    /**
     * 原子指标描述
     */
    private String indicatorsDes;
}
