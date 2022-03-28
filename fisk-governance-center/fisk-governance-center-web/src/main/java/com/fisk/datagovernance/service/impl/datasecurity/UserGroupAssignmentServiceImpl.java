package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.UserGroupAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupAssignmentPO;
import com.fisk.datagovernance.mapper.datasecurity.UserGroupAssignmentMapper;
import com.fisk.datagovernance.service.datasecurity.UserGroupAssignmentService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class UserGroupAssignmentServiceImpl extends ServiceImpl<UserGroupAssignmentMapper, UserGroupAssignmentPO> implements UserGroupAssignmentService {


    @Override
    public UserGroupAssignmentDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(UserGroupAssignmentDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(UserGroupAssignmentDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}