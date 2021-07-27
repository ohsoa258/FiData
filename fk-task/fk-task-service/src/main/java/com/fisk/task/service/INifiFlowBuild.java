package com.fisk.task.service;

import com.fisk.common.entity.BusinessResult;

/**
 * @author gy
 */
public interface INifiFlowBuild {

    /**
     * 创建nifi流程
     * 源到目标数据流程
     * @return 业务返回结果
     */
    BusinessResult<Object> buildSourceToTargetDataFlow();
}
