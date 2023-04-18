package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.mdm.dto.process.AllApprovalDTO;
import com.fisk.mdm.dto.process.EndingApprovalDTO;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.dto.process.ProcessApplyDTO;
import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.vo.process.AllApprovalVO;
import com.fisk.mdm.vo.process.EndingApprovalVO;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;
/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
public interface IProcessApplyService extends IService<ProcessApplyPO> {
    Page<ProcessApplyVO> getMyProcessApply(ProcessApplyDTO dto);
    Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto);

    Page<AllApprovalVO> getAllApproval(AllApprovalDTO dto);
    Page<EndingApprovalVO> getOverApproval(EndingApprovalDTO dto);
}
