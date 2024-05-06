package com.fisk.datamanagement.dto.email;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EmailServerDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;

    /**
     * 邮件服务器
     */
    @ApiModelProperty(value = "邮件服务器")
    public String emailServer;

}
