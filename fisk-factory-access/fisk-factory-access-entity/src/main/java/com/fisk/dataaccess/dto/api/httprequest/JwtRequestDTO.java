package com.fisk.dataaccess.dto.api.httprequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description bug: 上游可能不是username/password来定义账户密码,此时的json-key是有问题的,到时候api调用也有问题
 * @date 2022/4/28 12:09
 */
@Data
public class JwtRequestDTO {

    @ApiModelProperty(value = "api账户key", required = true)
    public String userKey;
    @ApiModelProperty(value = "api账户", required = true)
    public String username;
    @ApiModelProperty(value = "api密码key", required = true)
    public String pwdKey;
    @ApiModelProperty(value = "api密码", required = true)
    public String password;
}
