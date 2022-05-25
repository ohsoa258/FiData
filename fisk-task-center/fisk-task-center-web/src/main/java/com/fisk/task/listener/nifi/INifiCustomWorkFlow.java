package com.fisk.task.listener.nifi;

import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;

public interface INifiCustomWorkFlow {

    public void deleteCustomWorkNifiFlow(NifiCustomWorkListDTO nifiCustomWorkListDTO);
}
