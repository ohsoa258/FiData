package com.fisk.taskfactory.dto.customworkflow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.taskfactory.vo.customworkflow.NifiCustomWorkflowVO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowPageDTO {
    public String where;
    public Page<NifiCustomWorkflowVO> page;
}
