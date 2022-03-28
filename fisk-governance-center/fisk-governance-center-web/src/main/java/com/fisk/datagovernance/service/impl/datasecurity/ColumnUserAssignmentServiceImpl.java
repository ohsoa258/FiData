package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.ColumnUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnUserAssignmentPO;
import com.fisk.datagovernance.mapper.datasecurity.ColumnUserAssignmentMapper;
import com.fisk.datagovernance.service.datasecurity.ColumnUserAssignmentService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class ColumnUserAssignmentServiceImpl extends ServiceImpl<ColumnUserAssignmentMapper, ColumnUserAssignmentPO> implements ColumnUserAssignmentService {


    @Override
    public ColumnUserAssignmentDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(ColumnUserAssignmentDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(ColumnUserAssignmentDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}