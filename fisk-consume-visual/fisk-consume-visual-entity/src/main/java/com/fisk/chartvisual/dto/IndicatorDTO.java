package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.IndicatorTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/29 15:30
 */
@Data
public class IndicatorDTO {

    private Integer id;
    private String fieldName;
    /**
     * 派生名字
     */
    private String deriveName;
    private IndicatorTypeEnum type;
    private String tableName;
    /**
     * 原子指标属性
     */
    private String calculationLogic;

    /**
     * 派生指标属性
     */
    private String timePeriod;
    private String whereTimeLogic;
}
