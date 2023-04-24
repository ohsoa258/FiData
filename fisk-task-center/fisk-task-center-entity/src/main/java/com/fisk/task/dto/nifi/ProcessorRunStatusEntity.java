package com.fisk.task.dto.nifi;

import com.davis.client.model.RevisionDTO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author gy
 */
public class ProcessorRunStatusEntity {
    @ApiModelProperty(value = "状态")
    public String state;
    @ApiModelProperty(value = "断开节点确认")
    public boolean disconnectedNodeAcknowledged;
    @ApiModelProperty(value = "修改")
    public RevisionDTO revision;
}
