package com.fisk.mdm.dto.codeRule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/6/23 14:14
 * @Version 1.0
 */
@Data
public class CodeRuleGroupDTO {

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    /**
     * 属性id
     */
    @ApiModelProperty(value = "属性id")
    private Integer attributeId;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String desc;
}
