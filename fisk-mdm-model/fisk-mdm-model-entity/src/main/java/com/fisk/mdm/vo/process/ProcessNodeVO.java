package com.fisk.mdm.vo.process;

import com.fisk.mdm.enums.ProcessNodeTypeEnum;
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
public class ProcessNodeVO {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "节点名称")
    private String name;

    @ApiModelProperty(value = "节点下标")
    private Integer levels;

    @ApiModelProperty(value = "设置类型")
    private Integer settype;

    @ApiModelProperty(value = "类型名称")
    private String typeName;

    @ApiModelProperty(value = "人员列表")
    private List<ProcessPersonVO> personList;
}
