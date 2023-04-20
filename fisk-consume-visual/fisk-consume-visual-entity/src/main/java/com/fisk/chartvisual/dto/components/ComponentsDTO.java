package com.fisk.chartvisual.dto.components;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class ComponentsDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "classId")
    private Integer classId;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "选项列表")
    private List<ComponentsOptionDTO> optionList;
}
