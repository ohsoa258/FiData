package com.fisk.system.vo.license;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 菜单VO
 * @date 2023/1/5 10:44
 */
@Data
public class MenuVO {
    /**
     * 菜单ID
     */
    @ApiModelProperty(value = "菜单ID")
    public int id;

    /**
     * 菜单名称
     */
    @ApiModelProperty(value = "菜单名称")
    public String name;
}
