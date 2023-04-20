package com.fisk.chartvisual.dto.components;

import com.fisk.chartvisual.dto.components.ComponentsOptionDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class SaveComponentsDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "classId")
    private Integer classId;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "选项")
    private ComponentsOptionDTO option;
}
