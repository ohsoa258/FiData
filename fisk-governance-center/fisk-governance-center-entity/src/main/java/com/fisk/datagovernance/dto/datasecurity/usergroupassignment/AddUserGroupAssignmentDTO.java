package com.fisk.datagovernance.dto.datasecurity.usergroupassignment;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AddUserGroupAssignmentDTO {

    public long userGroupId;

    public List<Integer> userIdList;

}