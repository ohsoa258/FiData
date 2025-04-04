package com.fisk.datamanagement.dto.standards;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
@Data
public class StandardsExportDTO {

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

    @ApiModelProperty(value = "值域范围")
    private String valueRange;

    @ApiModelProperty(value = "被引用数据源数量")
    private Integer num;

    @ApiModelProperty(value = "被引用数据源")
    List<StandardsBeCitedDTO> standardsBeCitedDTOList;
}
