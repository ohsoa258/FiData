package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.ConfigureDTO;
import com.fisk.dataservice.dto.UserApiDTO;
import com.fisk.dataservice.dto.UserConfigureDTO;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ConfigureUserPO;

import java.util.List;

/**
 * @author wangyan
 */
public interface ConfigureUserService {

    /**
     * 分页查询用户
     * @param page
     * @param downSystemName
     * @return
     */
    Page<UserDTO> listData(Page<ConfigureUserPO> page,String downSystemName);

    /**
     * 保存用户服务
     * @param dto
     * @return
     */
    ResultEnum saveUserConfigure(UserConfigureDTO dto);

    /**
     * 添加用户
     * @param dto
     * @return
     */
    ResultEnum saveUser(ConfigureUserPO dto);

    /**
     * 编辑用户
     * @param dto
     * @return
     */
   ResultEnum updateUser(UserDTO dto);

    /**
     * 根据主键id删除用户
     * @param id
     * @return
     */
    ResultEnum deleteUserById(Integer id);

    /**
     * 删除该用户下的APi服务
     * @param dto
     * @return
     */
    ResultEnum deleteUserApiById(UserConfigureDTO dto);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    ConfigureUserPO byUserId(Integer id);

    /**
     * 根据用户id查询下的所有服务
     * @param id
     * @param currentPage
     * @param pageSize
     * @return
     */
    Page<ConfigureDTO> configureByUserId(Integer id,Integer currentPage, Integer pageSize);

    /**
     * 根据用户id查询下的所有服务
     * @param id
     * @return
     */
    List<UserApiDTO> configureByUserId(Integer id);
}
