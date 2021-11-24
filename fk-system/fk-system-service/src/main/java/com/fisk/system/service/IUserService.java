package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.*;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.dto.userinfo.UserPowerDTO;
import com.fisk.system.dto.userinfo.UserQueryDTO;

import java.util.List;

/**
 * @author Lock
 */
public interface IUserService {

    /**
     * 当前信息是否存在
     *
     * @param data data
     * @return 执行结果
     */
    Boolean exist(String data);

    /**
     * 添加用户
     * @param dto dto
     * @param dto
     * @return 返回值
     */
    ResultEnum register(UserDTO dto);

    /**
     * 获取用户详情
     *
     * @param id
     * @return 用户详情
     */
    UserDTO getUser(int id);

    /**
     * 删除用户
     *
     * @param id
     * @return 返回值
     */
    ResultEnum deleteUser(int id);

    /**
     * 编辑用户
     *
     * @param dto
     * @return 返回值
     */
    ResultEnum updateUser(UserDTO dto);

    /**
     * 查询用户
     *
     * @param username username
     * @param password password
     * @return 查询结果
     */
    UserDTO queryUser(String username, String password);

    /**
     * 用户列表
     * @param query
     * @return
     */
    Page<UserDTO> listUserData(UserQueryDTO query);

    /**
     * 查询用户
     *
     * @param dto dto
     * @return 查询结果
     */
    Page<UserPowerDTO> getPageUserData(QueryDTO dto);

    /**
     * 获取当前登录人信息
     * @return 登录用户名
     */
    UserInfoCurrentDTO getCurrentUserInfo();

    /**
     * 修改用户密码
     * @param dto
     * @return 返回值
     */
    ResultEnum changePassword(ChangePasswordDTO dto);

    /**
     * 获取用户表相关字段
     * @return
     */
    List<FilterFieldDTO> getUserInfoColumn();

}
