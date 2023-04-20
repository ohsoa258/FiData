package com.fisk.chartvisual.dto.contentsplit;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/16 14:07
 */
@Data
public class ChildvisualDTO {

    @ApiModelProperty(value = "表id")
    private Integer chartId;
    @ApiModelProperty(value = "目录")
    private String content;

    @ApiModelProperty(value = "组成背景")
    private String componentBackground;

    @ApiModelProperty(value = "安置组成背景")
    private String layComponentBackground;

    @ApiModelProperty(value = "逻辑删除")
    private Integer delFlag;
}
