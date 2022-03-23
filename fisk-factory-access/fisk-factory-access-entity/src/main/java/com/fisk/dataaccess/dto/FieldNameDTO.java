package com.fisk.dataaccess.dto;

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
    @ApiModelProperty(value = "源字段", required = true)
    public String sourceFieldName;
    @ApiModelProperty(value = "字段", required = true)
    public String fieldName;
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;
    @ApiModelProperty(value = "字段长度", required = true)
    public String fieldLength;
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;
    @ApiModelProperty(value = "物理表id", required = true)
    public int tableAccessId;
    @ApiModelProperty(value = "1是主键，0非主键", required = true)
    public int isPrimarykey;
    @ApiModelProperty(value = "0是实时物理表，1是非实时物理表", required = true)
    public int isRealtime;
}
