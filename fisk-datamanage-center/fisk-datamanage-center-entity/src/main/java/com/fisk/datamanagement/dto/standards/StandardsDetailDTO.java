package com.fisk.datamanagement.dto.standards;

import com.fisk.datamanagement.enums.ValueRangeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-07-25
 * @Description:
 */
@Data
public class StandardsDetailDTO {
    @ApiModelProperty(value = "Id")
    private Integer Id;

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "中文名称")
    private String name;

    @ApiModelProperty(value = "类型:1:目录 2:数据")
    private Integer type;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "英文名称")
    private String englishName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "数据元编号")
    private String datametaCode;

    @ApiModelProperty(value = "质量规则")
    private String qualityRule;

    @ApiModelProperty(value = "值域范围类型 1数据集 2数值 3数值范围")
    private ValueRangeTypeEnum valueRangeType;

    @ApiModelProperty(value = "符号")
    private String symbols;

    @ApiModelProperty(value = "值域范围")
    private String valueRange;

    @ApiModelProperty(value = "值域范围")
    private String valueRangeMax;
}
