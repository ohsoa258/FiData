package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则搜索条件
 * @date 2022/4/12 18:17
 */
@Data
public class DataCheckRuleSearchWhereMapVO {
    /**
     * 文本
     */
    @ApiModelProperty(value = "文本")
    public Object text;

    /**
     * 值
     */
    @ApiModelProperty(value = "值")
    public Object value;
}
