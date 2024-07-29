package com.fisk.mdm.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CAndLDTO {

    /**
     * 数据分类枚举集合
     */
    @ApiModelProperty(value = "数据分类枚举集合")
    private List<ClassificationsAndLevelsDTO> classifications;

    /**
     * 数据分级枚举集合
     */
    @ApiModelProperty(value = "数据分级枚举集合")
    private List<ClassificationsAndLevelsDTO> levels;

}
