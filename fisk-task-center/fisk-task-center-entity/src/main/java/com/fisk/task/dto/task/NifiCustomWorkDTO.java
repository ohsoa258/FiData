package com.fisk.task.dto.task;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class NifiCustomWorkDTO {

    //节点
    @ApiModelProperty(value = "节点")
    public BuildNifiCustomWorkFlowDTO NifiNode;
    //输入
    @ApiModelProperty(value = "输入")
    public List<BuildNifiCustomWorkFlowDTO> inputDucts;
    //输出
    @ApiModelProperty(value = "输出")
    public List<BuildNifiCustomWorkFlowDTO> outputDucts;

}
