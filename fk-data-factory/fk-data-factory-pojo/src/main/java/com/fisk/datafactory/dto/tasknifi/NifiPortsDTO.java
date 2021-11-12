package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class NifiPortsDTO {
    public List<NifiCustomWorkflowDetailDTO> inports;
    public List<NifiCustomWorkflowDetailDTO> outports;
}
