package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * @return
     */
    List<UserDTO> listData(Page<ConfigureUserPO> page);
}
