package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
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
     * 状态：已发布（1）、未发布（0）
     */
    public int status;

}
