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
}
