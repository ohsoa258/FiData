package com.fisk.task.service;

import com.fisk.common.entity.BusinessResult;

/**
 * @author gy
 */
public interface INifiFlowBuild {

    /**
     * 创建nifi流程
     * 源到目标数据流程
     */
    public BusinessResult<Object> buildSourceToTargetDataFlow();
}
