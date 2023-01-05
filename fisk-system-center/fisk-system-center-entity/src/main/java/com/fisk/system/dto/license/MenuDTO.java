package com.fisk.system.dto.license;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 菜单DTO
 * @date 2023/1/5 10:44
 */
@Data
public class MenuDTO {
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
