package com.fisk.datamodel.dto.widetablefieldconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigsDTO {
    /**
     * 宽表配置主表id
     */
    @ApiModelProperty(value = "宽表配置主表id")
    public Integer wideTableId;
    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;
    /**
     * 表类型：0维度 1事实
     */
    @ApiModelProperty(value = "表类型：0维度 1事实")
    public Integer tableType;
    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;
    /**
     * 字段类型长度
     */
    @ApiModelProperty(value = "字段类型长度")
    public Integer fieldLength;
    /**
     * 字段别名
     */
    @ApiModelProperty(value = "字段别名")
    public String alias;
}
