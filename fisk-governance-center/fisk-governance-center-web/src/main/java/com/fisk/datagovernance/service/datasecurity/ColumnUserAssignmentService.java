package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.columnuserassignment.ColumnUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnUserAssignmentPO;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
public interface ColumnUserAssignmentService extends IService<ColumnUserAssignmentPO> {


    /**
     * 批量添加列级安全关联
     * @param columnSecurityId
     * @param dtoList
     * @return
     */
    ResultEnum saveColumnUserAssignment(long columnSecurityId, List<ColumnUserAssignmentDTO> dtoList);

    /**
     * 获取列级配置关联用户
     * @param columnSecurityId
     * @return
     */
    List<ColumnUserAssignmentDTO> listColumnUserAssignment(long columnSecurityId);

}

