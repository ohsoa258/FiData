package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;

public interface INifiCustomWorkFlow {

    public void deleteCustomWorkNifiFlow(NifiCustomWorkListDTO nifiCustomWorkListDTO);

    ResultEnum suspendCustomWorkNifiFlow(String nifiCustomWorkflowId, boolean ifFire);
}
