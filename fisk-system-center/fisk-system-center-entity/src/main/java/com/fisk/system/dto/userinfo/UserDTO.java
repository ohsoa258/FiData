package com.fisk.system.dto.userinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author Lock
 */
@Data
public class UserDTO {

    @ApiModelProperty(value = "id")
    public Long id;

    @ApiModelProperty(value = "邮件")
    public String email;

    @ApiModelProperty(value = "用户账户")
    public String userAccount;

    @ApiModelProperty(value = "用户名称")
    public String username;

    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "创建者")
    public String createUser;
    /**
     * 是否有效
     */
    @ApiModelProperty(value = "是否有效")
    public boolean valid;

}
