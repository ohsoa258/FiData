package com.fisk.mdm.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程
 */
@Data
@NoArgsConstructor
public class ProcessInfoDTO {

    @ApiModelProperty(value = "实体ID")
    public int entityId;

    @ApiModelProperty(value = "异常处理")
    public int exceptionhandling;

    @ApiModelProperty(value = "自动审批")
    public int autoapproal;

    @ApiModelProperty(value = "是否启用")
    public int enable;

    @ApiModelProperty(value = "流程节点")
    public List<ProcessNodeDTO> processNodes;
}
