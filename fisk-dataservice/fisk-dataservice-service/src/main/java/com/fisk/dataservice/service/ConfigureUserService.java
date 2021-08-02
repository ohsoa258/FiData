package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.vo.UserVO;
import com.fisk.dataservice.entity.ConfigureUserPO;

import java.util.List;

/**
 * @author wangyan
 */
public interface ConfigureUserService {

    /**
     * 分页查询用户
     * @param page
     * @return
     */
    List<UserVO> listData(Page<ConfigureUserPO> page);

    /**
     * 保存用户
     * @param dto
     * @return
     */
    ResultEnum saveUser(ConfigureUserPO dto,String apiName);

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
}
