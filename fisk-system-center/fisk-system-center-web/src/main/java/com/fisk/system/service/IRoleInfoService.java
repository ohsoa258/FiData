package com.fisk.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.roleinfo.RoleInfoQueryDTO;
import com.fisk.system.dto.roleinfo.RolePowerDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IRoleInfoService{

    /**
     * 获取角色列表
     * @return 查询结果
     */
    Page<RoleInfoDTO> listRoleData(RoleInfoQueryDTO query);

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
     * @return
     */
    List<FilterFieldDTO> getRoleInfoColumn();

}
