package com.fisk.system.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.RoleServiceAssignmentDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IRoleServiceAssignmentService {
    /**
     * 根据角色id，获取所有服务
     *
     * @param roleId dto
     * @return 保存结果
     */
    List<RoleServiceAssignmentDTO> getRoleServiceList(int roleId);
    /**
     * 保存角色选中服务列表
     *
     * @param dto dto
     * @return 保存结果
     */
    ResultEnum addRoleServiceAssignment(AssignmentDTO dto);
}
