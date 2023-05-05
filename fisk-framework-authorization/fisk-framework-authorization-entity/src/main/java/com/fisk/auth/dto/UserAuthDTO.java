package com.fisk.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lock
 *
 * 用于接收白泽登录对象
 */
@Data
@NoArgsConstructor
public class UserAuthDTO {

    @ApiModelProperty(value = "临时id,用于其他服务获取token")
    private Long temporaryId;

    @ApiModelProperty(value = "用户账户")
    private String userAccount;

    @ApiModelProperty(value = "密码")
    private String password;

}


