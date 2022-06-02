package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验类型
 * @date 2022/6/2 17:59
 */
@Data
public class DataCheckTypeV0 {
    /**
     * 文本
     */
    @ApiModelProperty(value = "文本")
    public String text;

    /**
     * 值
     */
    @ApiModelProperty(value = "值")
    public int value;

    /**
     * 父级id
     */
    @ApiModelProperty(value = "父级id")
    public int parentId;

    /**
     * 等级
     */
    @ApiModelProperty(value = "等级：1 校验类型 2 数据校验类型")
    public int grade;
}
