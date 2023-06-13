package com.fisk.task.service.pipeline;

import com.fisk.common.core.response.ResultEnum;

public interface INifiSchedulingComponentService {
    ResultEnum runOnce(Long nifiCustomWorkflowDetailId);
}
