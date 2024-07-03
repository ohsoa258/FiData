package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDTO {
    @ApiModelProperty(value = "主键id", required = true)
    public long id;

    @ApiModelProperty(value = "事实表id", required = true)
    public int factId;
    /**
     * 事实表中文字段名称
     */
    @ApiModelProperty(value = "事实表中文字段名称")
    public String factFieldCnName;
    /**
     * 事实表字段类型
     */
    @ApiModelProperty(value = "事实表字段类型")
    public String factFieldType;
    /**
     * 事实表字段长度
     */
    @ApiModelProperty(value = "事实表字段长度")
    public int factFieldLength;
    /**
     * 事实表字段描述
     */
    @ApiModelProperty(value = "事实表字段描述")
    public String factFieldDes;

    /**
     * 事实表英文字段名称
     */
    @ApiModelProperty(value = "事实表英文字段名称")
    public String factFieldEnName;
    /**
     * 属性类型：0:退化事实，1:事实建，2:度量字段
     */
    @ApiModelProperty(value = "属性类型：0:退化事实，1:事实建，2:度量字段", required = true)
    public int attributeType;
    /**
     * 关联维度表id
     */
    @ApiModelProperty(value = "关联维度表id", required = true)
    public int associateDimensionId;
    /**
     * 关联维度字段id
     */
    @ApiModelProperty(value = "关联维度字段id", required = true)
    public int associateDimensionFieldId;
    /**
     * 源表名称
     */
    @ApiModelProperty(value = "源表名称")
    public String sourceTableName;
    /**
     * 源字段名称
     */
    @ApiModelProperty(value = "源字段名称")
    public String sourceFieldName;

    @ApiModelProperty(value = "配置详情(事实key的json配置详情)")
    public String configDetails;

    //该变量名虽为isBusinessKey，但实际在doris和postgre库时 是作为建表主键字段  前端名改为业务主键标识
    @ApiModelProperty(value = "是否是主键  1：是  0：不是")
    public int isBusinessKey;

    //该变量名虽为isPrimaryKey，但由于历史原因，其实是作为业务覆盖标识
    @ApiModelProperty(value = "是否是业务覆盖标识  1：是  0：不是")
    public int isPrimaryKey;

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
     * 数仓贯标关联对象（指标或数据元）
     */
    @ApiModelProperty(value = "数仓贯标关联对象（指标或数据元）")
    public List<FieldsAssociatedMetricsOrMetaObjDTO> associatedDto;

    @ApiModelProperty(value = "创建人")
    public String createUser;

    /**
     * 数据分类：DataClassificationEnum
     * PUBLIC_DATA(1, "公开数据", "green"),
     * INTERNAL_DATA(2, "内部数据", "blue"),
     * MAX(3, "敏感数据", "orange"),
     * MIN(4, "高度敏感数据", "red"),
     */
    @ApiModelProperty(value = "数据分类：DataClassificationEnum")
    public Integer dataClassification;

    /**
     * 数据分级：DataLevelEnum
     * LEVEL1(1, "一级（一般数据）", "green"),
     * LEVEL2(2, "二级（重要数据）", "blue"),
     * LEVEL3(3, "三级（敏感数据）", "orange"),
     * LEVEL4(4, "四级（核心数据）", "red"),
     */
    @ApiModelProperty(value = "数据分级：DataLevelEnum")
    public Integer dataLevel;

}
