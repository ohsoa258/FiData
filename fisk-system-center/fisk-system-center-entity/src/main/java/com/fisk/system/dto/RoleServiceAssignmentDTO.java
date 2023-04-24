package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class RoleServiceAssignmentDTO {

    @ApiModelProperty(value = "角色Id")
    public int roleId;

    @ApiModelProperty(value = "服务Id")
    public int serviceId;

}
