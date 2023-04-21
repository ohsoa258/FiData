package com.fisk.datafactory.dto.customworkflow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowPageDTO {
    @ApiModelProperty(value = "where")
    public String where;
    @ApiModelProperty(value = "page")
    public Page<NifiCustomWorkflowVO> page;
}
