package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:40
 */
@Data
@TableName("tb_derived_indicators")
public class DerivedIndicatorsPO extends BasePO {

    /**
     * 事实表id
     */
    private Integer factId;
    /**
     * 派生指标名称
     */
    private String derivedName;
    /**
     * 派生指标描述
     */
    private String derivedDes;
    /**
     * 原子指标id
     */
    private Integer atomicId;
    /**
     * 时间周期
     */
    private String timePeriod;
}
