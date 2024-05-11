package com.fisk.task.dto.modelpublish;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishFieldDTO {
    /**
     * 维度表字段id
     */
    @ApiModelProperty(value = "维度表字段id")
    public long fieldId;
    /**
     * 维度表英文字段名称
     */
    @ApiModelProperty(value = "维度表英文字段名称")
    public String fieldEnName;
    /**
     * 维度表字段类型
     */
    @ApiModelProperty(value = "维度表字段类型")
    public String fieldType;
    /**
     * 维度表字段长度
     */
    @ApiModelProperty(value = "维度表字段长度")
    public int fieldLength;
    /**
     * 属性类型：0：维度属性
     */
    @ApiModelProperty(value = "属性类型：0：维度属性")
    public int attributeType;
    /**
     * 是否业务主键 0:否 1:是
     */
    @ApiModelProperty(value = "是否业务主键 0:否 1:是")
    public int isPrimaryKey;
    /**
     * 源字段名称
     */
    @ApiModelProperty(value = "源字段名称")
    public String sourceFieldName;
    /**
     * 关联维度id
     */
    @ApiModelProperty(value = "关联维度id")
    public int associateDimensionId;
    /**
     * 关联维度表名称
     */
    @ApiModelProperty(value = "关联维度表名称")
    public String associateDimensionName;
    /**
     * 关联维度表SQL脚本
     */
    @ApiModelProperty(value = "关联维度表SQL脚本")
    public String associateDimensionSqlScript;
    /**
     * 关联维度字段id
     */
    @ApiModelProperty(value = "关联维度字段id")
    public int associateDimensionFieldId;
    /**
     * 关联维度表字段名称
     */
    @ApiModelProperty(value = "关联维度表字段名称")
    public String associateDimensionFieldName;
    /**
     * 字段精度
     */
    @ApiModelProperty(value = "字段精度")
    public Integer fieldPrecision;

    /**
     * 事实表、维度表在数仓建模sql预览时公用：是否是业务覆盖标识 0:否 1:是
     */
    @ApiModelProperty(value = "是否是业务覆盖标识 0:否 1:是")
    public int isBusinessKey;

    /**
     * 是否是doris分区字段 1：是 0：不是
     */
    @ApiModelProperty(value = "是否是doris分区字段 1：是 0：不是")
    public int isPartitionKey;

    /**
     * doris分区类型 RANGE 或 LIST
     */
    @ApiModelProperty(value = "doris分区类型 RANGE 或 LIST")
    public String dorisPartitionType;

    /**
     * doris分区属性值
     */
    @ApiModelProperty(value = "doris分区属性值")
    public String dorisPartitionValues;

    /**
     * 是否是doris分桶字段 1：是 0：不是
     */
    @ApiModelProperty(value = "是否是doris分桶字段 1：是 0：不是")
    public int isDistributedKey;

    /**
     * 是否doris聚合key  0否 1是
     */
    @ApiModelProperty(value = "是否doris聚合key  0否 1是")
    public int isAggregateKey;

    /**
     * doris聚合模型，聚合函数类型：
     * SUM：求和，多行的 Value 进行累加。
     * REPLACE：替代，下一批数据中的 Value 会替换之前导入过的行中的 Value。
     * MAX：保留最大值。
     * MIN：保留最小值。
     * REPLACE_IF_NOT_NULL：非空值替换。和 REPLACE 的区别在于对于null值，不做替换。
     * HLL_UNION：HLL 类型的列的聚合方式，通过 HyperLogLog 算法聚合。
     * BITMAP_UNION：BIMTAP 类型的列的聚合方式，进行位图的并集聚合。
     */
    @ApiModelProperty(value = "doris聚合模型，聚合函数类型：...")
    public String aggregateType;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    public String fieldDes;

}
