package com.fisk.datagovernance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.RowUserAssignmentPO;
import com.fisk.datagovernance.mapper.RowUserAssignmentMapper;
import com.fisk.datagovernance.service.RowUserAssignmentService;
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
public class RowUserAssignmentServiceImpl extends ServiceImpl<RowUserAssignmentMapper, RowUserAssignmentPO> implements RowUserAssignmentService {


    @Override
    public RowUserAssignmentDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(RowUserAssignmentDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(RowUserAssignmentDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}