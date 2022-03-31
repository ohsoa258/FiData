package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.RowUserAssignmentPO;
import com.fisk.datagovernance.mapper.datasecurity.RowUserAssignmentMapper;
import com.fisk.datagovernance.service.datasecurity.RowUserAssignmentService;
import org.springframework.stereotype.Service;

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