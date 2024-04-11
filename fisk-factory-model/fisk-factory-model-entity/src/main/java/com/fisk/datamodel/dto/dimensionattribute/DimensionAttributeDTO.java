package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.factattribute.FieldsAssociatedMetricsOrMetaObjDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeDTO {
    @ApiModelProperty(value = "id")
    public long id;

    /**
     * 维度表id
     */
    @ApiModelProperty(value = "维度表id")
    public int dimensionId;
    /**
     * 维度表中文字段名称
     */
    @ApiModelProperty(value = "维度表中文字段名称")
    public String dimensionFieldCnName;
    /**
     * 维度表字段类型
     */
    @ApiModelProperty(value = "维度表字段类型")
    public String dimensionFieldType;
    /**
     * 维度表字段长度
     */
    @ApiModelProperty(value = "维度表字段长度")
    public int dimensionFieldLength;
    /**
     * 维度表字段描述
     */
    @ApiModelProperty(value = "维度表字段描述")
    public String dimensionFieldDes;
    /**
     * 维度表英文字段名称
     */
    @ApiModelProperty(value = "维度表英文字段名称")
    public String dimensionFieldEnName;
    /**
     * 属性类型：0：维度属性
     */
    @ApiModelProperty(value = "属性类型：0：维度属性")
    public int attributeType;
    /**
     * 关联维度表id
     */
    @ApiModelProperty(value = "关联维度表id")
    public int associateDimensionId;
    /**
     * 关联维度字段id
     */
    @ApiModelProperty(value = "关联维度字段id")
    public int associateDimensionFieldId;
    /**
     * 是否业务主键 0:否 1:是
     */
    //注意 由于历史原因  维度表和事实表的该变量名含义相反  在维度表是  这里就是业务主键
    @ApiModelProperty(value = "是否业务主键 0:否 1:是")
    public int isPrimaryKey;
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
    /**
     * 配置详情(维度key的json配置详情)
     */
    @ApiModelProperty(value = "配置详情(维度key的json配置详情)")
    public String configDetails;

    //注意 由于历史原因  维度表和事实表的该变量名含义相反  在维度表是  这里就是业务覆盖标识
    @ApiModelProperty(value = "是否是业务覆盖标识  1：是  0：不是")
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
     * 数仓贯标关联对象（指标或数据元）
     */
    @ApiModelProperty(value = "数仓贯标关联对象（指标或数据元）")
    public List<FieldsAssociatedMetricsOrMetaObjDTO> associatedDto;

}
