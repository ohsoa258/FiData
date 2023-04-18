package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.process.*;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.vo.process.*;

import javax.servlet.http.HttpServletResponse;

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
     * @param entityId
     * @param batchNumber
     * @param eventTypeEnum
     * @return
     */
    ResultEnum addProcessApply(Integer entityId,String description, String batchNumber, EventTypeEnum eventTypeEnum);

    /**
     * 获取我的待审核流程
     *
     * @return
     */
    Page<ProcessApplyVO> getMyProcessApply(ProcessApplyDTO dto);

    /**
     * 获取待处理审批列表
     * @param dto
     * @return
     */
    Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto);
    /**
     * 获取所有审批列表
     * @param dto
     * @return
     */
    Page<AllApprovalVO> getAllApproval(AllApprovalDTO dto);

    /**
     * 获取已处理审批列表
     * @param dto
     * @return
     */
    Page<EndingApprovalVO> getOverApproval(EndingApprovalDTO dto);

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

    /**
     * 批量审批
     * @param dto
     * @return
     */
    ResultEnum batchApproval(BatchApprovalDTO dto);

    /**
     * 撤回审批
     * @param applyId
     * @return
     */
    ResultEnum rollbackApproval(Integer applyId);

    /**
     * 下载当前流程记录
     * @param applyId
     * @param response
     */
    void downloadApprovalApply(Integer applyId, HttpServletResponse response);
}
