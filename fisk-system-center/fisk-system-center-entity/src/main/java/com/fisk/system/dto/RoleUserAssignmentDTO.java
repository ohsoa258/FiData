package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class RoleUserAssignmentDTO {

    @ApiModelProperty(value = "角色Id")
    public int roleId;

    @ApiModelProperty(value = "用户Id")
    public int userId;

}
