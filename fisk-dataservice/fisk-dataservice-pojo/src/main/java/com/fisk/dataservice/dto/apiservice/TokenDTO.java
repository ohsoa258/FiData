package com.fisk.dataservice.dto.apiservice;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description token DTO
 * @date 2022/1/6 14:51
 */
public class TokenDTO {
    /**
     * 应用账号
     */
    @ApiModelProperty(value = "应用账号")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appAccount;

    /**
     * 应用密码
     */
    @ApiModelProperty(value = "应用密码")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String appPassword;
}
