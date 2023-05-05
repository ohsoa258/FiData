package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class NifiPortsDTO {
    @ApiModelProperty(value = "输入")
    public List<NifiCustomWorkflowDetailDTO> inports;
    @ApiModelProperty(value = "输出")
    public List<NifiCustomWorkflowDetailDTO> outports;
}
