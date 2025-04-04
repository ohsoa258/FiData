package com.fisk.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.roleinfo.RoleInfoQueryDTO;
import com.fisk.system.dto.roleinfo.RolePowerDTO;
import com.fisk.system.entity.RoleInfoPO;
import com.fisk.system.vo.roleinfo.RoleInfoVo;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IRoleInfoService extends IService<RoleInfoPO> {

    /**
     * 获取角色列表
     *
     * @param query
     * @return 查询结果
     */
    Page<RoleInfoDTO> listRoleData(RoleInfoQueryDTO query);
    /**
     * 获取角色列表
     */
    List<RoleInfoDTO>getAllRole();

    /**
     * 添加角色
     *
     * @param dto dto
     * @return 保存结果
     */
    ResultEnum addRole(RoleInfoDTO dto);

    /**
     * 删除角色
     *
     * @param id id
     * @return 保存结果
     */
    ResultEnum deleteRole(int id);

    /**
     * 根据id获取角色详情
     *
     * @param id id
     * @return 保存结果
     */
    RoleInfoDTO getRoleById(int id);

    /**
     * 根据ids获取角色详情
     *
     * @param ids ids
     * @return 保存结果
     */
    List<RoleInfoDTO> getRoleByIds(List<Integer> ids);

    /**
     * 角色编辑
     *
     * @param dto dto
     * @return 保存结果
     */
    ResultEnum updateRole(RoleInfoDTO dto);

    /**
     * 权限管理--分页获取角色列表
     *
     * @param dto dto
     * @return 保存结果
     */
    IPage<RolePowerDTO> getPageRoleData(QueryDTO dto);

    /**
     * 获取角色字段数据
     *
     * @return
     */
    List<FilterFieldDTO> getRoleInfoColumn();

    /**
     * 获取所有角色及角色下用户信息
     *
     * @return
     */
    List<RoleInfoVo> getTreeRols();

    /**
     * 根据用户id获取用户的角色信息
     *
     * @return
     */
    List<RoleInfoDTO> getRolebyUserId(int userId);

    /**
     * 根据角色名获取角色id
     *
     * @param roleName
     * @return
     */
    RoleInfoDTO getRoleByRoleName(String roleName);
}
