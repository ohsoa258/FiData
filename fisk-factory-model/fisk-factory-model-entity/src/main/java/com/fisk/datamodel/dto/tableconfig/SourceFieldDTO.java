package com.fisk.datamodel.dto.tableconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SourceFieldDTO {
    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "字段名")
    public String fieldName;

    @ApiModelProperty(value = "字段详细信息")
    public String fieldDes;

    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    @ApiModelProperty(value = "字段长度")
    public int fieldLength;

    @ApiModelProperty(value = "首要键")
    public int primaryKey;

    @ApiModelProperty(value = "属性类型")
    public int attributeType;
    /**
     * 是否关联维度
     */
    @ApiModelProperty(value = "是否关联维度")
    public boolean associatedDim;
    /**
     * 关联维度表id
     */
    @ApiModelProperty(value = "关联维度表id")
    public int associatedDimId;
    /**
     * 关联维度表名称
     */
    @ApiModelProperty(value = "关联维度表名称")
    public String associatedDimName;
    /**
     * 关联维度字段表字段id
     */
    @ApiModelProperty(value = "关联维度字段表字段id")
    public int associatedDimAttributeId;
    /**
     * 关联维度字段表字段名称
     */
    @ApiModelProperty(value = "关联维度字段表字段名称")
    public String associatedDimAttributeName;
    /**
     * 来源表
     */
    @ApiModelProperty(value = "来源表")
    public String sourceTable;
    /**
     * 来源字段
     */
    @ApiModelProperty(value = "来源字段")
    public String sourceField;
    /**
     * 聚合逻辑
     */
    @ApiModelProperty(value = "聚合逻辑")
    public String calculationLogic;
    /**
     * 原子指标id
     */
    @ApiModelProperty(value = "原子指标id")
    public int atomicId;

}
