package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorPushDTO {
    /**
     * 字段类型：0 退化指标 1维度键 2 原子指标 3 派生指标
     */
    @ApiModelProperty(value = "字段类型：0 退化指标 1维度键 2 原子指标 3 派生指标")
    public int attributeType;
    /**
     * 退化维度字段名称
     */
    @ApiModelProperty(value = "退化维度字段名称")
    public String factFieldName;
    /**
     * 退化维度字段类型
     */
    @ApiModelProperty(value = "退化维度字段类型")
    public String factFieldType;
    /**
     * 退化维度字段类型长度
     */
    @ApiModelProperty(value = "退化维度字段类型长度")
    public int factFieldLength;

    /**
     * 关联维度表id
     */
    @ApiModelProperty(value = "关联维度表id")
    public long dimensionTableId;

    /**
     * 关联维度名称
     */
    @ApiModelProperty(value = "关联维度名称")
    public String dimensionTableName;

    /**
     * 原子指标名称
     */
    @ApiModelProperty(value = "原子指标名称")
    public String atomicIndicatorName;
    /**
     * 聚合字段
     */
    @ApiModelProperty(value = "聚合字段")
    public String aggregatedField;
    /**
     * 聚合逻辑:sum、avg...
     */
    @ApiModelProperty(value = "聚合逻辑:sum、avg...")
    public String aggregationLogic;

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 原子指标id
     */
    @ApiModelProperty(value = "原子指标id")
    public int atomicId;

}
