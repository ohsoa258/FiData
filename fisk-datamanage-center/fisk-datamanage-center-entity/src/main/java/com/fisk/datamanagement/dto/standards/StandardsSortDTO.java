package com.fisk.datamanagement.dto.standards;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: wangjian
 * @Date: 2023-11-28
 * @Description:
 */
public class StandardsSortDTO {
    @ApiModelProperty(value = "当前菜单id")
    private Integer menuId;
    @ApiModelProperty(value = "目标菜单id 为空则是放在第一位")
    private Integer tragetId;

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    public Integer getTragetId() {
        return tragetId;
    }

    public void setTragetId(Integer tragetId) {
        this.tragetId = tragetId;
    }
}
