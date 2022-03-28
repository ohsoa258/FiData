package com.fisk.datagovernance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.UserGroupAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupAssignmentPO;
import com.fisk.datagovernance.mapper.UserGroupAssignmentMapper;
import com.fisk.datagovernance.service.UserGroupAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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