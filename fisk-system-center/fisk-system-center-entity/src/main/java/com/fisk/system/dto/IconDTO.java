package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class IconDTO {
    @ApiModelProperty(value = "题目")
    public String title;
    @ApiModelProperty(value = "缓存")
    public boolean noCache;
    @ApiModelProperty(value = "图标")
    public String icon;
}
