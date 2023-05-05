package com.fisk.system.dto.userinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserValidDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "有效的")
    public boolean valid;
}
