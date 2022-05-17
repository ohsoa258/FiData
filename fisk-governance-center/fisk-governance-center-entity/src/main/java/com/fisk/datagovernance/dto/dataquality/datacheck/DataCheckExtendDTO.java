package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.datagovernance.enums.dataquality.CheckTypeEnum;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则扩展属性
 * @date 2022/4/2 11:04
 */
public class DataCheckExtendDTO {
    /**
     * 数据校验规则id
     */
    @ApiModelProperty(value = "数据校验规则id")
    public int ruleId;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度")
    public int fieldLength;

    /**
     * 字段聚合波动阈值模板；
     * 字段聚合函数：
     * SUM、COUNT、AVG、MAX、MIN
     */
    @ApiModelProperty(value = "字段聚合函数")
    public String fieldAggregate;

    /**
     * 字段校验模板；
     * 校验类型，多选逗号分割：
     * 1、唯一校验
     * 2、非空校验
     * 3、长度校验
     */
    @ApiModelProperty(value = "校验类型，多选逗号分割 1、唯一校验 2、非空校验 3、长度校验")
    public String checkType;

    /**
     * 权重、比例
     */
    @ApiModelProperty(value = "权重、比例")
    public float scale;

    /**
     * 表血缘断裂校验模板；
     * 上下游血缘关系范围：
     * 1、上游 2、下游 3、上下游
     */
    @ApiModelProperty(value = "上下游血缘关系范围")
    public int consanguinityRange;

    /**
     * 表行数波动阈值模板；
     * 表行数
     */
    @ApiModelProperty(value = "表行数")
    public int tbaleRow;
}
