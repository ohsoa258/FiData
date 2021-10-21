package com.fisk.datafactory.vo.customworkflow;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 *
 * 应用注册分页对象
 */
@Data
public class NifiCustomWorkflowVO {

    public long id;
    public String workflowId;
    /**
     * 管道名称
     */
    public String workflowName;
    /**
     * 是否发布
     */
    public int status;
    /**
     * 创建时间
     */
    public LocalDateTime createTime;
    public LocalDateTime updateTime;

}
