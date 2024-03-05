package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class SaveRoleServiceAssignmentDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "列表")
    public List<RoleServiceAssignmentDTO> list;
}
