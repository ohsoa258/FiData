package com.fisk.system.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.system.dto.UserDTO;
import com.fisk.system.entity.UserPO;

import java.util.List;

/**
 * @author Lock
 */
public interface UserMapper extends FKBaseMapper<UserPO> {

    /**
     * 获取所有用户列表
     *
     * @return 查询结果
     */
    List<UserDTO> userList();
}
