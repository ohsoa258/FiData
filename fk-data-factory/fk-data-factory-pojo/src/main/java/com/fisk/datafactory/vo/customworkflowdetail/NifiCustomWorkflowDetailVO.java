package com.fisk.datafactory.vo.customworkflowdetail;

import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowDetailVO {
    /**
     * 管道对象
     */
    public NifiCustomWorkflowDTO dto;
    /**
     * 管道详情对象
     */
    public List<NifiCustomWorkflowDetailDTO> list;
}
