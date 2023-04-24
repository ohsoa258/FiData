package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ChangePasswordDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 原密码
     */
    @ApiModelProperty(value = "原始密码")
    public String originalPassword;

    @ApiModelProperty(value = "密码")
    public String password;
}
