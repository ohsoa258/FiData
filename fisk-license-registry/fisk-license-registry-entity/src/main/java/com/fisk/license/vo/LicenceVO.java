package com.fisk.license.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description Licence
 * @date 2022/11/10 15:45
 */
@Data
public class LicenceVO {
    /**
     * 许可证
     */
    @ApiModelProperty(value = "许可证")
    public String licence;

    /**
     * Mac地址
     */
    @ApiModelProperty(value = "Mac地址")
    public String mac;

    /**
     * 平台
     */
    @ApiModelProperty(value = "平台")
    public String platform;

    /**
     * 授权人
     */
    @ApiModelProperty(value = "授权人")
    public String authorizer;

    /**
     * 菜单列表
     */
    @ApiModelProperty(value = "菜单列表")
    public List<String> menus;

    /**
     * 菜单名称列表
     */
    @ApiModelProperty(value = "菜单名称列表")
    public List<String> menuNames;

    /**
     * 许可证授权时间
     */
    @ApiModelProperty(value = "许可证授权时间")
    public String authDate;

    /**
     * 许可证过期时间
     */
    @ApiModelProperty(value = "许可证过期时间")
    public String expireTime;
}
