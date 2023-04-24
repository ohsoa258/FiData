package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.IndicatorTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/29 15:30
 */
@Data
public class IndicatorDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;
    /**
     * 派生名字
     */
    @ApiModelProperty(value = "派生名字")
    private String deriveName;
    @ApiModelProperty(value = "类型")
    private IndicatorTypeEnum type;
    @ApiModelProperty(value = "表名")
    private String tableName;
    /**
     * 原子指标属性
     */
    @ApiModelProperty(value = "原子指标属性")
    private String calculationLogic;

    /**
     * 派生指标属性
     */
    @ApiModelProperty(value = "派生指标属性")
    private String timePeriod;

    @ApiModelProperty(value = "fieldName")
    private String whereTimeLogic;
}
