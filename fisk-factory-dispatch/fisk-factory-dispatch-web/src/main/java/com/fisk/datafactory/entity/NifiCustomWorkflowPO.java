package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_nifi_custom_workflow")
public class NifiCustomWorkflowPO extends BasePO {

    /**
     * GUID
     */
    public String workflowId;
    /**
     * 管道名称
     */
    public String workflowName;
    /**
     * 负责人
     */
    public String pr;
    /**
     * 描述
     */
    @TableField("`desc`")
    public String desc;
    /**
     * 组件节点
     */
    public String listNode;
    /**
     * 组件、连线
     */
    public String listEdge;
    /**
     * 已发布（1）、未发布（0）、发布失败（2）、正在发布（3）
     */
    public int status;

}
