package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用密码重置 DTO
 * @date 2022/1/6 14:51
 */
public class AppPwdResetDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @Length(min = 0, max = 50, message = "长度最多50")
    @NotNull()
    public String appPassword;
}
