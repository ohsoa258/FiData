package com.fisk.datamodel.dto.factattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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

    //该变量名虽为isPrimaryKey，但由于历史原因，其实是作为业务覆盖标识，影响 是作为建表主键字段  前端名改为业务主键标识
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
}
