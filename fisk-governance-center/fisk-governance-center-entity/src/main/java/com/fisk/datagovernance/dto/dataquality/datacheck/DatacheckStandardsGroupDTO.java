package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-04-23
 * @Description:
 */
@Data
public class DatacheckStandardsGroupDTO {

    private Integer id;

    @ApiModelProperty(value = "校验组名称")
    private String checkGroupName;

    @ApiModelProperty(value = "数据标准MenuId")
    private Integer standardsMenuId;

    @ApiModelProperty(value = "数据元标准id")
    private Integer standardsId;

    @ApiModelProperty(value = "属性中文名称")
    private String chineseName;

    @ApiModelProperty(value = "属性英文名称")
    private String englishName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "数据元编号")
    private String datametaCode;

    List<DataCheckEditDTO> dataCheckList;
}
