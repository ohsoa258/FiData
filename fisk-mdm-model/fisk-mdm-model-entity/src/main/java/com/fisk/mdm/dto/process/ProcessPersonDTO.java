package com.fisk.mdm.dto.process;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程节点人员
 */
@Data
@NoArgsConstructor
public class ProcessPersonDTO {

    /**
     * 审批人类型,(1角色,2用户)
     */
    public int type;

    /**
     * 用户ID或角色ID
     */
    public int urid;
}
