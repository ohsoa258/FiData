package com.fisk.datamanagement.dto.email;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EmailUserDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;

    /**
     * 邮箱地址
     */
    @ApiModelProperty(value = "邮箱地址")
    private String emailAddress;

}
