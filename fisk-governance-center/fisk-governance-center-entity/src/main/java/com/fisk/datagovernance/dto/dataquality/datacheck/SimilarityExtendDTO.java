package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验模块下相似度组件扩展属性
 * @date 2022/4/2 11:04
 */
public class SimilarityExtendDTO {
    /**
     * 数据校验Id
     */
    @ApiModelProperty(value = "数据校验Id")
    public int datacheckId;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 权重、比例
     */
    @ApiModelProperty(value = "权重、比例")
    public int scale;
}
