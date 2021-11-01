package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_indicators")
@EqualsAndHashCode(callSuper = true)
public class IndicatorsPO extends BasePO {

    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 指标类型：0原子指标、1派生指标
     */
    public int indicatorsType;
    /**
     * 事实表id
     */
    public int factId;
    /**
     * 事实字段表id
     */
    public int factAttributeId;
    /**
     * 聚合逻辑
     */
    public String calculationLogic;
    /**
     * 指标中文名称
     */
    public String indicatorsCnName;
    /**
     * 指标英文名称
     */
    public String indicatorsName;
    /**
     * 原子指标描述
     */
    public String indicatorsDes;
    /**
     * 原子指标id
     */
    public int atomicId;
    /**
     * 时间周期
     */
    public String timePeriod;
    /**
     * 派生指标类型：0基于原子公式、1指标公式
     */
    public int derivedIndicatorsType;
    /**
     * 指标公式
     */
    public String indicatorsFormula;
    /**
     * 业务限定id
     */
    public int businessLimitedId;

}
