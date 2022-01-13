package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;


/**
 * @author dick
 * @version v1.0
 * @description 应用 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class AppRegisterDTO
{
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appName;

    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    @Length(min = 0, max = 255, message = "长度最多255")
    public String appDesc;

    /**
     * 应用负责人
     */
    @ApiModelProperty(value = "应用负责人")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appPrincipal;

    /**
     * 应用账号
     */
    @ApiModelProperty(value = "应用账号")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appPassword;
}
