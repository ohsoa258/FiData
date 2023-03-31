package com.fisk.mdm.vo.process;

import com.fisk.mdm.enums.ProcessPersonTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程节点人员
 */
@Data
@NoArgsConstructor
public class ProcessPersonVO {

    @ApiModelProperty(value = "主键")
    public int id;

    @ApiModelProperty(value = "流程节点ID")
    public int rocessNodeId;

    @ApiModelProperty(value = "审批人类型,(角色,用户)")
    public int type;

    @ApiModelProperty(value = "审批人类型名称,(角色,用户)")
    public String typeName;

    @ApiModelProperty(value = "用户ID或角色ID")
    public int urid;

    @ApiModelProperty(value = "用户名称或角色名称")
    public String urName;
}
