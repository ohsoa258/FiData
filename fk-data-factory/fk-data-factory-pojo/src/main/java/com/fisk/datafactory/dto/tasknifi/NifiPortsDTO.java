package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class NifiPortsDTO {
    public List<NifiCustomWorkflowDetailPO> inports;
    public List<NifiCustomWorkflowDetailPO> outports;
}
