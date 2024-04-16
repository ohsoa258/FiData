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

    @ApiModelProperty(value = "是否启用添加")
    public int switchAdd;

    @ApiModelProperty(value = "是否启用修改")
    public int switchUpdate;

    @ApiModelProperty(value = "是否启用删除")
    public int switchDelete;

    @ApiModelProperty(value = "是否授权(指标管理用)")
    public int switchAuthorization;
}
