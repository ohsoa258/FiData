package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.usergroupassignment.AddUserGroupAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupassignment.UserGroupAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupAssignmentPO;
import com.fisk.system.dto.QueryDTO;
import com.fisk.system.dto.userinfo.UserGroupQueryDTO;
import com.fisk.system.dto.userinfo.UserPowerDTO;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
public interface UserGroupAssignmentService extends IService<UserGroupAssignmentPO> {

    /**
     * 分页获取系统用户信息
     * @param dto
     * @return
     */
    Page<UserPowerDTO> getPageUserData(UserGroupQueryDTO dto);

    /**
     * 用户组添加系统用户
     * @param dto
     * @return
     */
    ResultEnum saveData(AddUserGroupAssignmentDTO dto);

    /**
     * 获取用户组下所有系统用户id
     * @param userGroupId
     * @return
     */
    List<Integer> getSelectedUser(long userGroupId);

}

