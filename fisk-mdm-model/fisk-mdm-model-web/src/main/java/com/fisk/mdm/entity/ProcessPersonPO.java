package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.mdm.enums.ProcessPersonTypeEnum;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description:
 */
@Data
@TableName("tb_process_person")
public class ProcessPersonPO extends ProcessPO {

    /**
     * 流程节点ID
     */
    private int rocessNodeId;

    /**
     * 审批人类型,(角色,用户)
     */
    private ProcessPersonTypeEnum type;

    /**
     * 用户ID或角色ID
     */
    private int urid;
}
