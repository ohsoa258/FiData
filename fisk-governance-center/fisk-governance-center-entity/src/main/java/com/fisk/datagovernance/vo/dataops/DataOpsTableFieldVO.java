package com.fisk.datagovernance.vo.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 字段信息
 * @date 2022/4/22 13:02
 */
@Data
public class DataOpsTableFieldVO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

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
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度")
    public int fieldLength;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    public String fieldDes;
}
