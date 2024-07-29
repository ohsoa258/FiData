package com.fisk.mdm.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ClassificationsAndLevelsDTO {

    /**
     * 枚举名称
     */
    @ApiModelProperty(value = "枚举名称")
    private String enumName;

    /**
     * 枚举值
     */
    @ApiModelProperty(value = "枚举值")
    private Integer enumValue;

    /**
     * 枚举级别/颜色
     */
    @ApiModelProperty(value = "枚举级别/颜色")
    private String enumLevel;

}
