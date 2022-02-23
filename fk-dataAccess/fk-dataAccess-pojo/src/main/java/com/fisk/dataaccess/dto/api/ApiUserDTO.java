package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 1.3
 * @description api用户实体类
 * @date 2022/2/23 11:31
 */
@Data
public class ApiUserDTO {

    @NotNull
    @ApiModelProperty(value = "api账户", required = true)
    public String useraccount;
    @NotNull
    @ApiModelProperty(value = "api密码", required = true)
    public String password;
}
