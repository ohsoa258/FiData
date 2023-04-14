package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.vo.process.EndingApprovalVO;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
public interface IProcessApplyService extends IService<ProcessApplyPO> {
    Page<ProcessApplyVO> getMyProcessApply(PendingApprovalDTO dto);
    Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto);
    Page<EndingApprovalVO> getOverApproval(PendingApprovalDTO dto);
}
