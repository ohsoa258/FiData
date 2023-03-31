package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.ProcessPersonTypeEnum;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description:
 */
@Data
@TableName("tb_process_person")
public class ProcessPersonPO extends BasePO {

    /**
     * 流程节点ID
     */
    public int rocessNodeId;

    /**
     * 审批人类型,(角色,用户)
     */
    public ProcessPersonTypeEnum type;

    /**
     * 用户ID或角色ID
     */
    public int urid;
}
