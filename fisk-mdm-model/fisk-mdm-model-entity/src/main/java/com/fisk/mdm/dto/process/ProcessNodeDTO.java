package com.fisk.mdm.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程节点
 */
@Data
@NoArgsConstructor
public class ProcessNodeDTO {

    @ApiModelProperty(value = "节点名称")
    private String name;

    @ApiModelProperty(value = "节点下标")
    private Integer levels;

    @ApiModelProperty(value = "设置类型")
    private Integer settype;

    @ApiModelProperty(value = "节点人员")
    private List<ProcessPersonDTO> personList;
}
