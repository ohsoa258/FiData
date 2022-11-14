package com.fisk.license.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 新增许可证
 * @date 2022/11/10 15:24
 */
@Data
public class LicenceDTO {
    /**
     * Mac地址
     */
    @NotNull()
    @ApiModelProperty(value = "Mac地址")
    public String mac;

    /**
     * 菜单列表
     */
    @NotNull()
    @ApiModelProperty(value = "菜单列表")
    public List<String> menus;

    /**
     * 到期时间
     */
    @NotNull()
    @ApiModelProperty(value = "到期时间，年月日格式 例如：2023/01/01")
    public String expireTime;
}
