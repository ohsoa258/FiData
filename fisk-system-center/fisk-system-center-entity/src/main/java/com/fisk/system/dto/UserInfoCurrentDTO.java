package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserInfoCurrentDTO {
    @ApiModelProperty(value = "id")
    public long id;
    @ApiModelProperty(value = "用户名称")
    public String userName;
    @ApiModelProperty(value = "用户账号")
    public String userAccount;
    @ApiModelProperty(value = "邮件")
    public String email;
}
