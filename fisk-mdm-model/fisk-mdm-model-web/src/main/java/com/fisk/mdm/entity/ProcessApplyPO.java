package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.ApprovalApplyStateEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 * @Description: 流程申请工单
 */
@Data
@TableName("tb_process_apply")
public class ProcessApplyPO extends BasePO {

    /**
     * 描述
     */
    private String description;

    /**
     * 流程ID
     */
    private int processId;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 申请时间
     */
    @TableField(value = "application_time", fill = FieldFill.INSERT)
    private LocalDateTime applicationTime;

    /**
     * 当前状态
     */
    private ApprovalApplyStateEnum state;

    /**
     * 当前审批节点
     */
    private int approverNode;

    /**
     * 当前批次code
     */
    private String fidataBatchCode;

    /**
     * 操作类型
     */
    private EventTypeEnum operationType;

    /**
     * 工单状态
     */
    private int opreationstate;
}
