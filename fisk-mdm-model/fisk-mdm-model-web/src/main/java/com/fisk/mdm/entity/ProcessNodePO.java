package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.ProcessNodeTypeEnum;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程节点
 */
@Data
@TableName("tb_process_node")
public class ProcessNodePO extends BasePO {

    /**
     * 节点名称
     */
    private String name;

    /**
     * 流程ID
     */
    private int processId;

    /**
     * 节点下标
     */
    private int levels;
    /**
     * 设置类型
     */
    private ProcessNodeTypeEnum settype;
}
