package com.fisk.system.dto.emailserver;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器编辑DTO
 * @date 2022/3/24 13:56
 */
@EqualsAndHashCode(callSuper = true)
public class EmailServerEditDTO extends EmailServerDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
