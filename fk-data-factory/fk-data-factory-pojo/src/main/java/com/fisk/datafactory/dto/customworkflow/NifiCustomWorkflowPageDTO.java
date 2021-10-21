package com.fisk.datafactory.dto.customworkflow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowPageDTO {
    public String where;
    public Page<NifiCustomWorkflowVO> page;
}
