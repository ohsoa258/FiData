package com.fisk.system.dto.userinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserDropDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "用户名")
    public String username;

}
