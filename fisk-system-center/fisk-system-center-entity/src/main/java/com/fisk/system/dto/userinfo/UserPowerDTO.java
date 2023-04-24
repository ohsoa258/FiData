package com.fisk.system.dto.userinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserPowerDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "邮件")
    public String email;

    @ApiModelProperty(value = "用户账号")
    public String userAccount;

    @ApiModelProperty(value = "用户姓名")
    public String username;

}
