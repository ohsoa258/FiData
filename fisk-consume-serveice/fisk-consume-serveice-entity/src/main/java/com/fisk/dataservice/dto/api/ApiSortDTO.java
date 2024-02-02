package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-29
 * @Description:
 */
@Data
public class ApiSortDTO {
    @ApiModelProperty(value = "当前菜单id")
    private Integer menuId;
    @ApiModelProperty(value = "目标菜单id 为空则是放在第一位")
    private Integer targetId;
    @ApiModelProperty(value = "是否跨级")
    private Boolean crossLevel;
    @ApiModelProperty(value = "pid")
    private Integer pid;
}
