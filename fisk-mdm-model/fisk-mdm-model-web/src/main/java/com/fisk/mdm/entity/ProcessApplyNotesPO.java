package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.mdm.enums.ApprovalNodeStateEnum;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 * @Description: 审批流程
 */
@Data
@TableName("tb_process_apply_notes")
public class ProcessApplyNotesPO extends ProcessPO {

    /**
     * 流程工单ID
     */
    private int processapplyId;

    /**
     * 流程节点ID
     */
    private int processnodeId;

    /**
     * 审批状态
     */
    private ApprovalNodeStateEnum state;

    /**
     * 审批描述
     */
    private String remark;
}
