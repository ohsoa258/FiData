package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/17 10:02
 */
@Data
@TableName("tb_indicators")
public class IndicatorsPO extends BasePO {

    /**
     * 业务域id
     */
    private Integer businessId;
    /**
     * 指标类型：0原子指标、1派生指标
     */
    private Integer indicatorsType;
    /**
     * 指标名称
     */
    private String indicatorsName;
    /**
     * 指标描述
     */
    private String indicatorsDes;
    /**
     * 事实表id
     */
    private Integer factId;
    /**
     * 事实字段表id
     */
    private Integer factAttributeId;
    /**
     * 原子指标id
     */
    private Integer atomicId;
    /**
     * 时间周期
     */
    private String timePeriod;
    /**
     * 聚合逻辑
     */
    private String calculationLogic;
    /**
     * 派生指标类型：0基于原子指标、1基于指标公
     */
    private Integer derivedIndicatorsType;
    /**
     * 指标公式
     */
    private String indicatorsFormula;
}
