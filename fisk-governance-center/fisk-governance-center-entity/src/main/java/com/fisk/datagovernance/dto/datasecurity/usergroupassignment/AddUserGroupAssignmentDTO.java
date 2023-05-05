package com.fisk.datagovernance.dto.datasecurity.usergroupassignment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AddUserGroupAssignmentDTO {

    @ApiModelProperty(value = "用户组ID")
    public long userGroupId;

    @ApiModelProperty(value = "用户ID列表")
    public List<Integer> userIdList;

}