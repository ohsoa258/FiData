package com.fisk.mdm.vo.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-04-10
 * @Description:
 */
@Data
public class PersonVO {
    @ApiModelProperty(value = "审核人")
    private String approval;
    @ApiModelProperty(value = "节点等级")
    private Integer levels;
    @ApiModelProperty(value = "节点名称")
    private String processNodeName;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "状态")
    private String state;
}
