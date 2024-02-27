package com.fisk.datamanagement.dto.standards;

import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.enums.ValueRangeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-20
 * @Description:
 */
@Data
public class StandardsDTO {
    @ApiModelProperty(value = "Id")
    private Integer Id;

    @ApiModelProperty(value = "menu_id")
    private Integer menuId;

    @ApiModelProperty(value = "中文名称")
    private String chineseName;

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

    @ApiModelProperty(value = "被引用数据源数量")
    private Integer num;

    @ApiModelProperty(value = "被引用数据源")
    List<StandardsBeCitedDTO> standardsBeCitedDTOList;

    @ApiModelProperty(value = "被引用代码集")
    List<CodeSetDTO> codeSetDTOList;
}
