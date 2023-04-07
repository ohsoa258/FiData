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
    private Integer entityId;

    /**
     * 异常处理
     */
    private Integer exceptionhandling;

    /**
     * 自动审批
     */
    private Integer autoapproal;

    /**
     * 是否启用
     */
    private Integer enable;

    /**
     * 流程节点
     */
    private List<ProcessNodeDTO> processNodes;
}
