package com.fisk.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.UserDTO;
import com.fisk.system.dto.UserInfoCurrentDTO;
import com.fisk.system.dto.UserPowerDTO;
import com.fisk.system.entity.UserPO;

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
     *
     * @param dto
     */
    ResultEnum register(UserDTO dto);

    /**
     * 获取用户详情
     *
     * @param id
     */
    UserDTO getUser(int id);

    /**
     * 删除用户
     *
     * @param id
     */
    ResultEnum deleteUser(int id);

    /**
     * 编辑用户
     *
     * @param dto
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
     *
     * @return 查询结果
     */
    List<UserDTO> listUserData();

    /**
     * 查询用户
     *
     * @param dto dto
     * @return 查询结果
     */
    IPage<UserPowerDTO> getPageUserData(QueryDTO dto);

    /**
     * 获取当前登录人信息
     * @return
     */
    UserInfoCurrentDTO getCurrentUserInfo();

}
