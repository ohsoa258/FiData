package com.fisk.datamanagement.dto.businessclassification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-28
 * @Description:
 */
@Data
public class BusinessCategorySortDTO {
    @ApiModelProperty(value = "当前菜单id")
    private Integer menuId;
    @ApiModelProperty(value = "目标菜单id 为空则是放在第一位")
    private Integer tragetId;
    @ApiModelProperty(value = "是否跨级")
    private Boolean crossLevel;
    @ApiModelProperty(value = "pid")
    private Integer pid;
}
