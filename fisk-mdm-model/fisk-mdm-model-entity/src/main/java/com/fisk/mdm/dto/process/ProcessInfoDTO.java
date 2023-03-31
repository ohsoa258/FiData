package com.fisk.mdm.dto.process;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程
 */
@Data
@NoArgsConstructor
public class ProcessInfoDTO {

    /**
     * 实体ID
     */
    public int entityId;

    /**
     * 异常处理
     */
    public int exceptionhandling;

    /**
     * 自动审批
     */
    public int autoapproal;

    /**
     * 是否启用
     */
    public int enable;

    /**
     * 流程节点
     */
    public List<ProcessNodeDTO> processNodes;
}
