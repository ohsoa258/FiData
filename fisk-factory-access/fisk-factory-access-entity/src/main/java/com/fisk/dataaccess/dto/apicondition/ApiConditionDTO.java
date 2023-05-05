package com.fisk.dataaccess.dto.apicondition;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiConditionDTO {
    /**
     * 类型名称
     */
    @ApiModelProperty(value = "类型名称")
    public String typeName;

    /**
     * 父级类型
     */
    @ApiModelProperty(value = "父级类型")
    public String parent;

    /**
     * 项
     */
    @ApiModelProperty(value = "项")
    public String item;

    /**
     * 说明
     */
    @ApiModelProperty(value = "说明")
    public String instructions;

    /**
     * 实例
     */
    @ApiModelProperty(value = "实例")
    public String instance;

}
