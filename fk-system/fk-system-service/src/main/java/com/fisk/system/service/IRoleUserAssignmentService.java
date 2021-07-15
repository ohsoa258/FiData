package com.fisk.system.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.dto.RoleUserAssignmentDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IRoleUserAssignmentService {

    /**
     * 根据角色id，获取所有用户
     *
     * @param roleId dto
     * @return 保存结果
     */
    List<RoleUserAssignmentDTO> getRoleUserList(int roleId);

    /**
     * 保存角色选中用户列表
     *
     * @param dto dto
     * @return 保存结果
     */
    ResultEnum addRoleUserAssignment(AssignmentDTO dto);

}
