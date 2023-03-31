package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.ApprovalStateEnum;
import com.fisk.mdm.enums.AutoapproalRuleEnum;
import com.fisk.mdm.enums.ExceptionApprovalEnum;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程信息
 */
@Data
@TableName("tb_process_info")
public class ProcessInfoPO extends BasePO {

    /**
     * 实体ID
     */
    public int entityId;

    /**
     * 异常处理
     */
    public ExceptionApprovalEnum exceptionhandling;

    /**
     * 自动审批
     */
    public AutoapproalRuleEnum autoapproal;

    /**
     * 是否启用
     */
    public ApprovalStateEnum enable;
}
