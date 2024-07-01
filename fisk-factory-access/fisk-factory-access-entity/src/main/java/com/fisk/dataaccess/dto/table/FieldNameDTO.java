package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class FieldNameDTO {
    @ApiModelProperty(value = "主键")
    public long id;
    @ApiModelProperty(value = "源表名", required = true)
    public String sourceTableName;
    @ApiModelProperty(value = "源字段名称", required = true)
    public String sourceFieldName;
    @ApiModelProperty(value = "源字段类型", required = true)
    public String sourceFieldType;
    @ApiModelProperty(value = "源字段类型长度", required = true)
    public String sourceFieldLength;
    @ApiModelProperty(value = "源字段类型精度", required = true)
    public Integer sourceFieldPrecision;
    @ApiModelProperty(value = "字段", required = true)
    public String fieldName;
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;
    @ApiModelProperty(value = "字段长度", required = true)
    public String fieldLength;
    @ApiModelProperty(value = "字段精度", required = true)
    public Integer fieldPrecision;
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;
    @ApiModelProperty(value = "物理表id", required = true)
    public int tableAccessId;
    @ApiModelProperty(value = "1是主键，0非主键", required = true)
    public int isPrimarykey;
    @ApiModelProperty(value = "0是实时物理表，1是非实时物理表", required = true)
    public int isRealtime;
    @ApiModelProperty(value = "物理表显示名称", required = true)
    public String displayName;

    /**
     * 数据分类：DataClassificationEnum
     * PUBLIC_DATA(1, "公开数据", "green"),
     * INTERNAL_DATA(2, "内部数据", "blue"),
     * MAX(3, "敏感数据", "orange"),
     * MIN(4, "高度敏感数据", "red"),
     */
    @ApiModelProperty(value = "数据分类")
    public Integer dataClassification;

    /**
     * 数据分级：DataLevelEnum
     * LEVEL1(1, "一级（一般数据）", "green"),
     * LEVEL2(2, "二级（重要数据）", "blue"),
     * LEVEL3(3, "三级（敏感数据）", "orange"),
     * LEVEL4(4, "四级（核心数据）", "red"),
     */
    @ApiModelProperty(value = "数据分级")
    public Integer dataLevel;

}
