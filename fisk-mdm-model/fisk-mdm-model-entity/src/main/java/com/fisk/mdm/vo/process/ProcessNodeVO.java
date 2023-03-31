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
    public int id;

    @ApiModelProperty(value = "节点名称")
    public String name;

    @ApiModelProperty(value = "节点下标")
    public int levels;

    @ApiModelProperty(value = "设置类型")
    public int settype;

    @ApiModelProperty(value = "类型名称")
    public String typeName;

    @ApiModelProperty(value = "人员列表")
    public List<ProcessPersonVO> personList;
}
