package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.masterdata.MasterDataDTO;
import com.fisk.mdm.dto.process.ApprovalDTO;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.vo.process.ApprovalDetailVO;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import com.fisk.mdm.vo.process.ProcessInfoVO;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程服务
 */
public interface ProcessService {

    /**
     * 保存流程
     * @param dto
     * @return
     */
    ResultEnum saveProcess(ProcessInfoDTO dto);

    /**
     * 获取流程VO
     * @param entityId
     * @return
     */
    ProcessInfoVO getProcess(Integer entityId);

    /**
     * 校验是否走流程
     *
     * @param entityId
     * @return
     * @throws FkException
     */
    ResultEnum verifyProcessApply(Integer entityId) throws FkException;

    /**
     * 添加工单
     * @param dto
     * @param batchNumber
     * @param eventTypeEnum
     * @return
     */
    ResultEnum addProcessApply(MasterDataDTO dto, String batchNumber, EventTypeEnum eventTypeEnum);

    /**
     * 获取我的待审核流程
     *
     * @return
     */
    Page<ProcessApplyVO> getMyProcessApply(PendingApprovalDTO dto);

    /**
     * 获取待处理审批列表
     * @param dto
     * @return
     */
    Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto);

    /**
     * 获取已处理审批列表
     * @param dto
     * @return
     */
    Page<PendingApprovalVO> getOverApproval(PendingApprovalDTO dto);

    /**
     * 获取审批流程详情
     * @param applyId
     * @return
     */
    ApprovalDetailVO getApprovalDetail(Integer applyId);

    /**
     * 审批
     * @param dto
     * @return
     */
    ResultEnum approval(ApprovalDTO dto);
}
