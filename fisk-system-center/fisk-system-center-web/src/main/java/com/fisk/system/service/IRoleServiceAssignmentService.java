package com.fisk.system.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.AssignmentDTO;
import com.fisk.system.dto.LoginServiceDTO;
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

    /**
     * 根据登录人id,获取服务列表
     *
     * @return 保存结果
     */
    List<LoginServiceDTO> getServiceList();

    /**
     * 获取菜单列表
     *
     * @return 结果
     */
    List<LoginServiceDTO> getAllMenuList();
}
