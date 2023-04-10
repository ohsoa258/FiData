package com.fisk.mdm.vo.process;

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
public class ProcessInfoVO {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "实体ID")
    private Integer entityId;

    @ApiModelProperty(value = "异常处理")
    private Integer exceptionhandling;

    @ApiModelProperty(value = "自动审批")
    private Integer autoapproal;

    @ApiModelProperty(value = "是否启用")
    private Integer enable;

    @ApiModelProperty(value = "流程节点")
    private List<ProcessNodeVO> processNodes;
}
