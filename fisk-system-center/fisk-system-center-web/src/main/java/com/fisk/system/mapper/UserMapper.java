package com.fisk.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.dto.userinfo.UserPageDTO;
import com.fisk.system.entity.UserPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Lock
 */
public interface UserMapper extends FKBaseMapper<UserPO> {

    /**
     * 获取用户列表
     *
     * @param page
     * @param dto
     * @return
     */
    Page<UserDTO> userList(Page<UserDTO> page, @Param("query") UserPageDTO dto);

    /**
     * 批量查询用户信息
     *
     * @param ids
     * @return 用户列表
     */
    List<UserDTO> getUserListByIds(@Param("ids") List<Long> ids);
}
